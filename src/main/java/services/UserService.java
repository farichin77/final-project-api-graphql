package services;

import io.restassured.response.Response;
import client.GraphQlClient;
import models.responses.user.MeResponse;
import utils.ApiResponse;
import utils.TestDataLoader;

public class UserService {

  public static ApiResponse<MeResponse> getMe() {
    String query = TestDataLoader.load("graphql/queries/Me.graphql");

    Response response = GraphQlClient.execute(query);

    MeResponse meResponse = response.as(MeResponse.class);

    return new ApiResponse<>(
        response.getStatusCode(),
        response.getHeaders(),
        meResponse
    );
  }
}
