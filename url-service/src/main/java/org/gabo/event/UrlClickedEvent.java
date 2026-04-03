package org.gabo.event;

import java.time.LocalDateTime;

public record UrlClickedEvent(String alias, String originalUrl, LocalDateTime clickedAt) {
}
