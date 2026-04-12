package org.gabo.domain.model.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import org.gabo.domain.model.UrlAnalysisResult;

public class UrlAnalysisResultMapper {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static UrlAnalysisResult parseResponse(String json) {
        try {
            return mapper.readValue(json, UrlAnalysisResult.class);
        } catch (Exception e) {
            throw new WebApplicationException("Error parsing AI response", 500);
        }
    }
}
