package com.exercise.vertx.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by chesterjavier on 6/2/20.
 */
@Data
@JsonPropertyOrder({"id", "sub_entities"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
public class Entry {

    @JsonProperty("id")
    private String id;

    @JsonProperty("sub_entities")
    private Set<Child> subEntities;

    public Entry(JsonObject json) {
        this.id = json.getString("id");
        this.subEntities = fetchJsonSubEntities(json.getJsonArray("sub_entities"));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("id", id)
                .put("sub_entities", subEntities);
        return json;
    }

    private Set<Child> fetchJsonSubEntities(JsonArray jsonArray) {
        return new HashSet(Arrays.asList(jsonArray));
    }


}
