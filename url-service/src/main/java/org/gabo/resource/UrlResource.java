package org.gabo.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.gabo.model.ShortUrl;
import org.gabo.service.UrlService;

import java.util.ArrayList;
import java.util.List;


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
    public Response create(CreateRequest req) {
        ShortUrl created = urlService.create(req.originalUrl(), req.alias());

        return Response.status(Response.Status.CREATED).entity(created).build();
    }


    /**
     * @return all urls
     */
    @GET
    public Response getAll(){
        return Response.ok(urlService.listAll()).build();
    }

    @GET
    @Path("/{alias}")
    public Response resolve(@PathParam("alias") String alias){
        ShortUrl url = urlService.resolve(alias);
        return Response.status(302)
                .header("Location", url.getOriginalUrl())
                .build();
    }

    @DELETE
    @Path("/{alias}")
    public Response delete(@PathParam("alias") String alias){
        urlService.softDelete(alias);
        return Response.noContent().build();
    }

}
