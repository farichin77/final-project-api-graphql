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
        String query = TestDataLoader.load("graphql/mutations/auth/Login.graphql");

        LoginVariables variables = new LoginVariables(email, password, companyId);

        Response response = GraphQlClient.execute(
                query,
                variables.toMap()
        );

        LoginResponse loginResponse = response.as(LoginResponse.class);
        String sid = response.getCookie("sid_b2b");

        // Fallback for manual parsing if getCookie fails
        if (sid == null) {
            for (String header : response.getHeaders().getValues("Set-Cookie")) {
                if (header.contains("sid_b2b=")) {
                    sid = header.split("sid_b2b=")[1].split(";")[0];
                    break;
                }
            }
        }

        // Only throw exception if login succeeded but no cookie even after fallback
        if (loginResponse.data != null && loginResponse.data.login.user != null && sid == null) {
            System.out.println("DEBUG: All headers: " + response.getHeaders().toString());
            throw new RuntimeException("Login succeeded but sid_b2b cookie missing");
        }

        // Set session cookie if login succeeded
        if (loginResponse.data != null && loginResponse.data.login.user != null) {
            AuthSession.setSessionCookie(sid);
        }

        return new ApiResponse<>(
                response.getStatusCode(),
                response.getHeaders(),
                loginResponse
        );
    }

    public static Response postLoginRaw(String email, String password, String companyId) {
        String query = TestDataLoader.load("graphql/mutations/auth/Login.graphql");

        LoginVariables variables = new LoginVariables(email, password, companyId);

        Response response = GraphQlClient.execute(
                query,
                variables.toMap()
        );

        LoginResponse loginResponse = response.as(LoginResponse.class);

        // Set session cookie if login succeeded
        if (loginResponse.data != null && loginResponse.data.login.user != null) {
            String sid = response.getCookie("sid_b2b");
            AuthSession.setSessionCookie(sid);
        }

        return response;
    }
}
