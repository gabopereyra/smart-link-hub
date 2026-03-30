package org.gabo.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestSecurity(user = "testuser", roles = {"user"})
class UrlResourceTest {
    @Test
    @Order(1)
    void create_withExplicitAlias_returns201() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    { "originalUrl": "https://example.com", "alias": "my-alias" }
                    """)
                .when()
                .post("/api/urls")
                .then()
                .statusCode(201)
                .body("alias",       equalTo("my-alias"))
                .body("originalUrl", equalTo("https://example.com"))
                .body("active",      equalTo(true));
    }

    @Test
    @Order(2)
    void create_withoutAlias_returns201AndGeneratesAlias() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    { "originalUrl": "https://auto-alias.com" }
                    """)
                .when()
                .post("/api/urls")
                .then()
                .statusCode(201)
                .body("alias",       notNullValue())
                .body("originalUrl", equalTo("https://auto-alias.com"));
    }

    @Test
    @Order(3)
    void create_withUrlWithoutScheme_returns201AndPrependsHttps() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    { "originalUrl": "no-scheme.com", "alias": "no-scheme" }
                    """)
                .when()
                .post("/api/urls")
                .then()
                .statusCode(201)
                .body("originalUrl", equalTo("https://no-scheme.com"));
    }

    @Test
    @Order(4)
    void create_withDuplicateAlias_returns409() {
        // Alias "my-alias" was created in test with @Order(1)
        given()
                .contentType(ContentType.JSON)
                .body("""
                    { "originalUrl": "https://other.com", "alias": "my-alias" }
                    """)
                .when()
                .post("/api/urls")
                .then()
                .statusCode(409);
    }

    @Test
    @Order(5)
    void list_defaultParams_returns200() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/urls")
                .then()
                .statusCode(200)
                .body("content",      notNullValue())
                .body("currentPage",  equalTo(0))
                .body("pageSize",     equalTo(10))
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(6)
    void list_withoutParams_returns200WithDefaults() {
        // page=0, size=0, service normalize size to 20
        given()
                .when()
                .get("/api/urls")
                .then()
                .statusCode(200)
                .body("pageSize", equalTo(20));
    }

    @Test
    @Order(7)
    void resolve_existingAlias_returns302WithLocation() {
        given()
                .redirects().follow(false)   // just check 302 status, not follow
                .when()
                .get("/api/urls/my-alias")
                .then()
                .statusCode(302)
                .header("Location", equalTo("https://example.com"));
    }

    @Test
    @Order(8)
    void resolve_nonExistingAlias_returns404() {
        given()
                .redirects().follow(false)
                .when()
                .get("/api/urls/does-not-exist")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(9)
    @TestSecurity(user = "testadmin", roles = {"admin"})
    void delete_existingAlias_returns204() {
        given()
                .when()
                .delete("/api/urls/my-alias")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(10)
    @TestSecurity(user = "testadmin", roles = {"admin"})
    void delete_alreadyDeletedAlias_returns409() {
        // "my-alias" was deleted in test Order(9), must return 409
        given()
                .when()
                .delete("/api/urls/my-alias")
                .then()
                .statusCode(409);
    }

    @Test
    @Order(11)
    @TestSecurity(user = "testadmin", roles = {"admin"})
    void delete_nonExistingAlias_returns404() {
        given()
                .when()
                .delete("/api/urls/ghost-alias")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(12)
    void resolve_deletedAlias_returns404() {
        // "my-alias" is inactive, throw AliasNotFoundException
        given()
                .redirects().follow(false)
                .when()
                .get("/api/urls/my-alias")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(13)
    void delete_existingAlias_returns403() {
        given()
                .when()
                .delete("/api/urls/my-alias")
                .then()
                .statusCode(403);
    }

}