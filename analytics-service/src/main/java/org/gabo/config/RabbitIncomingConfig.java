package org.gabo.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "rabbitmq")
public interface RabbitIncomingConfig {
    String host();
    int port();
    String username();
    String password();
    String exchange();
    String queue();

    @WithName("routing-key")
    String routingKey();
}