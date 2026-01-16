package client;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import config.EnvConfig;

import java.util.Map;

public class GraphQlClient {

    public static Response execute(String query) {
        return execute(query, Map.of());
    }

    public static Response execute(String query, Object variables) {
        String sessionCookie = AuthSession.getSessionCookie();
        
        RequestSpecification request = RestAssured.given()
                .baseUri(EnvConfig.BASE_URL)
                .header("Origin", "https://lms-b2b.do.dibimbing.id") // Matches browser
                .contentType(ContentType.JSON);

        boolean isLogin = query.contains("mutation Login");
        if (sessionCookie != null && !sessionCookie.isEmpty() && !isLogin) {
            request = request.header("Cookie", "sid_b2b=" + sessionCookie);
        }

        Response response = request
                .body(Map.of(
                        "query", query,
                        "variables", variables
                ))
                .log().all()
                .when()
                .post("/graphql");

        ValidatableResponse validatableResponse = response.then();
        validatableResponse.log().all();

        return validatableResponse.extract().response();
    }
}
