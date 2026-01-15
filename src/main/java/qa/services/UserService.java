package qa.services;

import io.restassured.response.Response;
import qa.client.GraphQlClient;
import qa.models.responses.user.MeResponse;
import qa.utils.ApiResponse;
import qa.utils.TestDataLoader;

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
