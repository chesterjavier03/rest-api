package com.exercise.vertx.service;

import java.util.Map;
import java.util.Set;

/**
 * Created by chesterjavier on 6/2/20.
 */
public interface Entity {

    String getId();

    Set getSubEntities();

    Map getData();
}
