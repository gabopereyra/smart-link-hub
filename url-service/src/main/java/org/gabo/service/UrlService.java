package org.gabo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.gabo.domain.OriginalUrl;
import org.gabo.domain.generator.AliasGenerator;
import org.gabo.dto.PaginatedResult;
import org.gabo.event.UrlClickedEvent;
import org.gabo.exception.AliasAlreadyExistsException;
import org.gabo.exception.AliasAlreadyInactiveException;
import org.gabo.exception.AliasGenerationException;
import org.gabo.exception.AliasNotFoundException;
import org.gabo.model.ShortUrl;
import org.gabo.repository.UrlRepository;

import java.time.LocalDateTime;

@ApplicationScoped
public class UrlService {
    private final UrlRepository urlRepository;
    private final AliasGenerator aliasGenerator;
    private final Emitter<UrlClickedEvent> emitter;

    public UrlService(UrlRepository urlRepository, AliasGenerator aliasGenerator, @Channel("url_clicked_out") Emitter<UrlClickedEvent> emitter) {
        this.urlRepository = urlRepository;
        this.aliasGenerator = aliasGenerator;
        this.emitter = emitter;
    }

    @Transactional
    public ShortUrl create(String originalUrl, String alias) {
        String finalAlias = (alias == null || alias.isBlank()) ? generateUniqueAlias() : alias.trim();

        try {
            if (urlRepository.existsByAlias(finalAlias)) {
                throw new AliasAlreadyExistsException(finalAlias);
            }

            var normalizedUrl = new OriginalUrl(originalUrl);

            var url = new ShortUrl();
            url.setOriginalUrl(normalizedUrl.getValue());
            url.setAlias(finalAlias);

            urlRepository.persist(url);
            return url;
        } catch (ConstraintViolationException e) {
            throw new AliasAlreadyExistsException(finalAlias);
        }
    }

    public ShortUrl resolve(String alias) {
        return urlRepository.findByAlias(alias)
                .filter(ShortUrl::isActive)
                .map(e -> {
                    var msg = new UrlClickedEvent(e.getAlias(), e.getOriginalUrl(), LocalDateTime.now());
                    emitter.send(msg);
                    return e;
                })
                .orElseThrow(() -> new AliasNotFoundException(alias));
    }

    public PaginatedResult listByPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = (size <= 0 || size > 100) ? 20 : size;
        return urlRepository.findPaginated(safePage, safeSize);
    }

    @Transactional
    public void softDelete(String alias) {
        ShortUrl url = urlRepository.findByAlias(alias).orElseThrow(() -> new AliasNotFoundException(alias));

        if (!url.isActive()) {
            throw new AliasAlreadyInactiveException(alias);
        }

        url.setActive(false);
    }

    private String generateUniqueAlias() {
        int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            String alias = aliasGenerator.generate();
            if (!urlRepository.existsByAlias(alias)) {
                return alias;
            }
        }
        throw new AliasGenerationException();
    }
}
