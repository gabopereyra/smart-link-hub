package org.gabo.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.gabo.exception.*;

import java.util.Map;

@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {


    @Override
    public Response toResponse(DomainException e) {
        int status = switch (e) {
            case AliasNotFoundException ignored -> 404;
            case AliasAlreadyExistsException ignored -> 409;
            case AliasAlreadyInactiveException ignored -> 409;
            case AliasGenerationException ignored -> 503;
            case UnsafeUrlException ignore -> 400;
            default -> 500;
        };
        return build(status, e.getMessage());
    }

    private Response build(int status, String message) {
        return Response.status(status)
                .entity(Map.of("error", message))
                .build();
    }
}
