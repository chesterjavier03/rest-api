package com.exercise.vertx.utility;

import com.exercise.vertx.service.Entity;

import java.util.Map;
import java.util.Set;

/**
 * Created by chesterjavier on 6/2/20.
 */
public abstract class AEntity implements Entity {

    public abstract String getId();

    public abstract Set getSubEntities();

    public abstract Map getData();


}
