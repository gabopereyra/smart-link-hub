package org.gabo.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;

import java.time.LocalDateTime;

@MongoEntity(collection = "clicks")
public class ClickEvent extends ReactivePanacheMongoEntity {
    public String alias;
    public String originalUrl;
    public LocalDateTime clickedAt;
}
