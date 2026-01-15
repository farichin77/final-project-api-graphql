package services;

import io.restassured.response.Response;
import client.AuthSession;
import client.GraphQlClient;
import config.CredentialsConfig;
import models.requests.login.LoginVariables;
import models.responses.login.LoginResponse;
import utils.ApiResponse;
import utils.TestDataLoader;

public class AuthService {

  public static ApiResponse<LoginResponse> postLogin() {
    return postLogin(
        CredentialsConfig.EMAIL,
        CredentialsConfig.PASSWORD,
        CredentialsConfig.COMPANY_ID
    );
  }

  public static ApiResponse<LoginResponse> postLogin(String email, String password, String companyId) {
    String query = TestDataLoader.load("graphql/mutations/Login.graphql");

    LoginVariables variables = new LoginVariables(email, password, companyId);

    Response response = GraphQlClient.execute(
        query,
        variables
    );

    LoginResponse loginResponse = response.as(LoginResponse.class);

    // Only throw exception if login succeeded but no cookie
    if (loginResponse.data.login.user != null && response.getCookie("sid_b2b") == null) {
      throw new RuntimeException("Login succeeded but sid_b2b cookie missing");
    }

    // Set session cookie if login succeeded
    if (loginResponse.data.login.user != null) {
      String sid = response.getCookie("sid_b2b");
      AuthSession.setSessionCookie(sid);
      System.out.println("SID: " + sid);
    }

    return new ApiResponse<>(
        response.getStatusCode(),
        response.getHeaders(),
        loginResponse
    );
  }
}
