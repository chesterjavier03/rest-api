package com.exercise.vertx.test;

import com.exercise.vertx.api.EntryApi;
import com.exercise.vertx.entity.Child;
import com.exercise.vertx.entity.Entry;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

/**
 * Created by chesterjavier on 6/2/20.
 */
@RunWith(VertxUnitRunner.class)
public class EntryApiTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntryApiTest.class);
    private Vertx vertx;
    private Integer port;
    private static MongodProcess MONGO;
    private static int MONGO_PORT = 12345;

    @BeforeClass
    public static void initialize() throws IOException {
        MongodStarter starter = MongodStarter.getDefaultInstance();

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
                .build();

        MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
        MONGO = mongodExecutable.start();
    }

    @AfterClass
    public static void shutdown() {
        MONGO.stop();
    }

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put("http.port", port)
                );

        vertx.deployVerticle(EntryApi.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void apiRoute_test(TestContext context) {
        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/api", response -> {
            response.handler(body -> {
                context.assertTrue(body.toString().contains("Hello"));
                async.complete();
            });
        });
    }

    @Test
    public void addEntry_test(TestContext context) {
        Async async = context.async();
        String id = "2";
        Child child1 = new Child();
        child1.setId("3");
        Child child2 = new Child();
        child2.setId("4");
        Set<Child> children = new HashSet<>();
        children.add(child1);
        children.add(child2);
        final String json = Json.encodePrettily(new Entry(id, children));
        vertx.createHttpClient().post(port, "localhost", "/api/entry")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(json.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        final Entry entry = Json.decodeValue(body.toString(), Entry.class);
                        context.assertNotNull("Success");
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }
}
