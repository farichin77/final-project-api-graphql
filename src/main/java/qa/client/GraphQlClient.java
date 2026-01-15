package qa.client;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import qa.config.EnvConfig;

import java.util.Map;

public class GraphQlClient {

  public static Response execute(String query) {
    return execute(query, Map.of());
  }

  public static Response execute(String query, Object vars) {
    boolean isDebug = EnvConfig.IS_DEBUG.equals("true");

    RequestSpecification requestSpecification = RestAssured.given()
        .baseUri(EnvConfig.BASE_URL)
        .contentType(ContentType.JSON);

    if (isDebug) {
      requestSpecification.log().all();
    }

    String sessionCookie = AuthSession.getSessionCookie();
    if (sessionCookie != null) {
      requestSpecification.cookie("sid_b2b", sessionCookie);
    }

    Response response = requestSpecification
        .body(Map.of(
            "query", query,
            "variables", vars
        ))
        .when()
        .post("/graphql");

    ValidatableResponse validatableResponse = response.then();
    if (isDebug) {
      validatableResponse.log().all();
    }

    return validatableResponse.extract().response();
  }
}
