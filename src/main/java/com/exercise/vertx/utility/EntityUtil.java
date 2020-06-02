package com.exercise.vertx.utility;

import com.exercise.vertx.entity.Entry;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sun.deploy.util.ArrayUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chesterjavier on 6/2/20.
 */
public class EntityUtil extends AEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityUtil.class);
    private ObjectMapper mapper = new ObjectMapper();
    private static FileWriter file;

    @SneakyThrows
    private List<Entry> constructModelEntry() {
        LOGGER.info("Construct model entries from json file....");
        return mapper.readValue(new File("entity.json"),
                TypeFactory.defaultInstance().constructCollectionType(List.class, Entry.class));
    }

    @Override
    public String getId() {
        LOGGER.info("Fetching id from initial entry...");
        return constructModelEntry().stream()
                .filter(en -> !en.getId().isEmpty()).findFirst().get().getId();
    }

    @Override
    public Set getSubEntities() {
        LOGGER.info("Fetching sub entries from Entry model...");
        return constructModelEntry().stream().map(Entry::getSubEntities).collect(Collectors.toSet());
    }

    @Override
    public Map getData() {
        return constructModelEntry().stream()
                .collect(Collectors.toMap(Entry::getId, Entry::getSubEntities));
    }

    public Entry fetchEntryById(String id) {
        LOGGER.info("Fetching id from initial entry...");
        return constructModelEntry().stream()
                .filter(en -> en.getId().equals(id)).findFirst().get();
    }

    public List<Entry> fetchAll() {
        LOGGER.info("Fetching all entries....");
        return constructModelEntry();
    }

    public String addEntry(Entry entry) {
        try {
            mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            String[] entity = mapper.readValue(new File("entity.json"), String[].class);
            JsonArray jsonArray = new JsonArray(Arrays.asList(entity));
            JsonObject json = new JsonObject();
            json.put("entry", mapper.writeValueAsString(entry));
            jsonArray.add(json);
            file = new FileWriter("entity.json");
            file.write(jsonArray.toString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Success";
    }
}
