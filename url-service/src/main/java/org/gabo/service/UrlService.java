package org.gabo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.gabo.model.ShortUrl;
import org.gabo.repository.UrlRepository;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UrlService {
    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository){
        this.urlRepository = urlRepository;
    }

    @Transactional
    public ShortUrl create(String originalUrl, String alias){
        String finalAlias = (alias == null || alias.isBlank()) ? generateAlias() : alias.trim();

        if (urlRepository.existsByAlias(finalAlias)) {
            throw new IllegalArgumentException("Already exists: " + finalAlias);
        }

        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
        }

        var url = new ShortUrl();
        url.setOriginalUrl(originalUrl);
        url.setAlias(finalAlias);

        urlRepository.persist(url);
        return url;
    }

    public ShortUrl resolve(String alias) {
        return urlRepository.findByAlias(alias)
                .filter(ShortUrl::isActive)
                .orElseThrow(() -> new NotFoundException("Alias not found: " + alias));
    }

    public List<ShortUrl> listAll() {
        return urlRepository.listAll();
    }

    @Transactional
    public void softDelete(String alias) {
        ShortUrl url = resolve(alias);
        url.setActive(false);
        urlRepository.persist(url);
    }

    private String generateAlias() {
        return UUID.randomUUID().toString().substring(0, 6);
    }
}
