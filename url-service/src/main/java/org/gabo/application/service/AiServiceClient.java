package org.gabo.application.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.gabo.domain.model.UrlAnalysisRequest;
import org.gabo.domain.model.UrlAnalysisResult;
import org.gabo.infrastructure.client.AiServiceApi;

import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class AiServiceClient {

    private final AiServiceApi aiServiceApi;

    public AiServiceClient(@RestClient AiServiceApi aiServiceApi){
        this.aiServiceApi = aiServiceApi;
    }

    @CircuitBreaker(
            requestVolumeThreshold = 5,
            failureRatio = 0.5,
            delay = 10,
            delayUnit = ChronoUnit.SECONDS
    )
    @Fallback(fallbackMethod = "isSafeFallback")
    public UrlAnalysisResult analyzeUrl(String url) {
        Log.info(">>> Calling AI service...");
        UrlAnalysisResult result = aiServiceApi.analyze(new UrlAnalysisRequest(url));
        Log.info(">>> AI service successful invocation");
        return result;
    }

    public UrlAnalysisResult isSafeFallback(String url) {
        Log.warn(">>> FALLBACK AI service for: " + url);
        return new UrlAnalysisResult(true, "AiService not available, assume url it is safe");
    }

}

