package org.gabo.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import org.gabo.domain.model.UrlAnalysisRequest;
import org.gabo.domain.model.UrlAnalysisResult;
import org.gabo.domain.port.out.UrlAnalyzerService;

import static org.gabo.domain.model.mapper.UrlAnalysisResultMapper.parseResponse;

@Path("/api/ai")
public class UrlAnalysisResource {
    private final UrlAnalyzerService urlAnalyzerService;

    public UrlAnalysisResource(UrlAnalyzerService urlAnalyzerService){
        this.urlAnalyzerService = urlAnalyzerService;
    }

    @POST
    @Path("/analyze")
    public UrlAnalysisResult analyze(UrlAnalysisRequest request) {
        String response = urlAnalyzerService.analyze(request.url());
        return parseResponse(response);
    }
}

