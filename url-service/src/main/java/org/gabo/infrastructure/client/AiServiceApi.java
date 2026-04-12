package org.gabo.infrastructure.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.gabo.domain.model.UrlAnalysisRequest;
import org.gabo.domain.model.UrlAnalysisResult;

@RegisterRestClient(configKey = "ai-service")
public interface AiServiceApi {
    @POST
    @Path("/api/ai/analyze")
    UrlAnalysisResult analyze(UrlAnalysisRequest request);
}

