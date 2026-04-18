package org.gabo.config;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class RabbitMQConfig {

    @Inject
    Vertx vertx;

    @Inject
    RabbitIncomingConfig config;

    void onStart(@Observes StartupEvent ev) {
        RabbitMQOptions options = new RabbitMQOptions()
                .setHost(config.host())
                .setPort(config.port())
                .setUser(config.username())
                .setPassword(config.password());

        RabbitMQClient client = RabbitMQClient.create(vertx, options);

        JsonObject queueArgs = new JsonObject().put("x-dead-letter-exchange", "url-clicked-dlq");

        client.start()
                .flatMap(v -> client.exchangeDeclare(config.exchange(), "direct", true, false))
                .flatMap(v -> client.queueDeclare(config.queue(), true, false, false, queueArgs))
                .flatMap(v -> client.queueBind(config.queue(), config.exchange(), config.routingKey()))
                .flatMap(v -> client.stop())
                .await().indefinitely();
    }
}