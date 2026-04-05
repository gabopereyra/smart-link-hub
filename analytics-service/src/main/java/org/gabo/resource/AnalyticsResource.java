package org.gabo.resource;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.bson.Document;
import org.gabo.model.ClickEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Path("api/analytics")
public class AnalyticsResource {
    private record AliasStats(String alias, int count, LocalDateTime lastUse) {
    }

    @GET
    @Path("/stats")
    public Uni<List<AliasStats>> getStats() {
        return ClickEvent.mongoCollection()
                .aggregate(List.of(
                        new Document("$group", new Document("_id", "$alias")
                                .append("count", new Document("$sum", 1))
                                .append("lastUse", new Document("$max", "$clickedAt")))
                ), Document.class)
                .map(doc -> new AliasStats(
                        doc.getString("_id"),
                        doc.getInteger("count"),
                        doc.get("lastUse", Date.class).toInstant()
                                .atZone(ZoneId.systemDefault()).toLocalDateTime()
                ))
                .collect().asList();
    }

    @GET
    @Path("/{alias}")
    public Uni<List<ClickEvent>> getByAlias(@PathParam("alias") String alias) {
        return ClickEvent.find("alias", alias).list();
    }
}
