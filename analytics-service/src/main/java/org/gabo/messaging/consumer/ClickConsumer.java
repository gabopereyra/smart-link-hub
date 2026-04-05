package org.gabo.messaging.consumer;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.gabo.messaging.event.UrlClickedEvent;
import org.gabo.model.ClickEvent;

@ApplicationScoped
public class ClickConsumer {
    @Incoming("url_clicked")
    public Uni<Void> consume(JsonObject payload) {
        var event = payload.mapTo(UrlClickedEvent.class);

        var clickEvent = new ClickEvent();
        clickEvent.alias = event.alias();
        clickEvent.originalUrl = event.originalUrl();
        clickEvent.clickedAt = event.clickedAt();

        return clickEvent.persist().replaceWithVoid();
    }
}
