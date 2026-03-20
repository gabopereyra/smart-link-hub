package org.gabo.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.gabo.dto.PaginatedResult;
import org.gabo.model.ShortUrl;

import java.util.Optional;

@ApplicationScoped
public class UrlRepository implements PanacheRepository<ShortUrl> {
    public Optional<ShortUrl> findByAlias(String alias){
        return find("alias", alias).firstResultOptional();
    }

    public boolean existsByAlias(String alias) {
        return count("alias", alias) > 0;
    }

    public PaginatedResult findPaginated(int page, int size) {
        PanacheQuery<ShortUrl> query = findAll().page(page, size);

        return new PaginatedResult(
                query.list(),
                page,
                size,
                query.count(),
                query.pageCount()
        );
    }
}
