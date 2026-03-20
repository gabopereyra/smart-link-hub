package org.gabo.dto;

import org.gabo.model.ShortUrl;

import java.util.List;

public record PaginatedResult(
        List<ShortUrl> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages
) {
}
