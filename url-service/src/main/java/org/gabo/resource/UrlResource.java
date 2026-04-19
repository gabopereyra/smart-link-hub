package org.gabo.resource;

import io.quarkus.logging.Log;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.gabo.model.ShortUrl;
import org.gabo.service.UrlService;


@Path("/api/urls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UrlResource {
    private final UrlService urlService;

    public UrlResource(UrlService urlService){
        this.urlService = urlService;
    }

    public record CreateRequest(String originalUrl, String alias) {}

    @POST
    @RolesAllowed("user")
    public Response create(CreateRequest req) {
        ShortUrl created = urlService.create(req.originalUrl(), req.alias());

        return Response.status(Response.Status.CREATED).entity(created).build();
    }


    @GET
    @RolesAllowed("user")
    public Response getByPage(
            @QueryParam("page") int page,
            @QueryParam("size") int size) {
        return Response.ok(urlService.listByPage(page, size)).build();
    }

    @GET
    @Path("/{alias}")
    @RolesAllowed("user")
    public Response resolve(@PathParam("alias") String alias){
        Log.infof("Resolving alias: %s", alias);
        ShortUrl url = urlService.resolve(alias);
        Log.infof("Redirecting to: %s", url.getOriginalUrl());

        return Response.status(302)
                .header("Location", url.getOriginalUrl())
                .build();
    }

    @DELETE
    @Path("/{alias}")
    @RolesAllowed("admin")
    public Response delete(@PathParam("alias") String alias){
        urlService.softDelete(alias);
        return Response.noContent().build();
    }

}
