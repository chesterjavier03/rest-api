package com.exercise.vertx.api;

import com.exercise.vertx.entity.Entry;
import com.exercise.vertx.utility.EntityUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by chesterjavier on 6/2/20.
 */
public class EntryApi extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryApi.class);

    private EntityUtil util = new EntityUtil();

    public static final String COLLECTION = "entries";

    private MongoClient mongo;

    @Override
    public void start(Future<Void> future) {

        Router router = Router.router(vertx);

        mongo = MongoClient.createShared(vertx, config());

        router.route("/api").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/html")
                    .end("<h1>Hello from my first Vertx application</h1>");
        });

        router.get("/api/entries").handler(this::fetchAll);
        router.route("/api/entry*").handler(BodyHandler.create());
        router.post("/api/entry").handler(this::addEntry);
        router.get("/api/entry/:id").handler(this::fetchOneEntry);
        router.get("/api/entry/sub/all").handler(this::fetchSubEntries);


        vertx.createHttpServer().requestHandler(router::accept).listen(config()
                .getInteger("http.port", 8080), result -> {
            if (result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });

    }

    private void fetchOneEntry(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        LOGGER.info("Fetching one entry with id = " + id);
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
//            Entry entry = util.fetchEntryById(id);
//            routingContext.response()
//                    .setStatusCode(200)
//                    .putHeader("content-type", "application/json; charset=utf-8")
//                    .end(Json.encodePrettily(entry));
            mongo.findOne(COLLECTION, new JsonObject().put("_id", id), null, ar -> {
                if (ar.succeeded()) {
                    if (ar.result() == null) {
                        routingContext.response().setStatusCode(404).end();
                        return;
                    }
                    Entry entry = new Entry(ar.result());
                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(entry));
                } else {
                    routingContext.response().setStatusCode(404).end();
                }
            });
        }

    }

    private void addEntry(RoutingContext routingContext) {
        LOGGER.info("Adding an entry....");
        final Entry entry = Json.decodeValue(routingContext.getBodyAsString(),
                Entry.class);

        mongo.insert(COLLECTION, entry.toJson(), r ->
                routingContext.response()
                        .setStatusCode(201)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(r.result())));

    }

    private void fetchAll(RoutingContext routingContext) {
        LOGGER.info("Fetching all....");
        mongo.find(COLLECTION, new JsonObject(), results -> {
            List<JsonObject> objects = results.result();
            List<Entry> whiskies = objects.stream().map(Entry::new).collect(Collectors.toList());
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(whiskies));
        });
    }
//        routingContext.response()
//                .putHeader("content-type", "application/json; charset=utf-8")
//                .end(Json.encodePrettily(util.fetchAll()));
//}

    private void fetchSubEntries(RoutingContext routingContext) {
        LOGGER.info("Fetching all sub entries....");
        Set subEntities = util.getSubEntities();
        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(subEntities));
    }

    @Override
    public void stop() {
        LOGGER.info("Shutting down application");
        mongo.close();
    }
}
