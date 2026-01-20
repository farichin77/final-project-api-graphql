package services;

import io.restassured.response.Response;
import client.GraphQlClient;
import models.requests.employee.AddEmployeeVariable;
import models.responses.employee.AddEmployeeResponse;
import utils.ApiResponse;
import utils.TestDataLoader;

import java.util.Map;

public class EmployeeService {

  public static ApiResponse<AddEmployeeResponse> addEmployee() {
    String query = TestDataLoader.load("graphql/mutations/employee/CreateEmployee.graphql");

    Map<String, Object> variable = AddEmployeeVariable.variables(
        "Bejo",
        "00101",
        "bejo111122sss@dibimbing.id",
        "76986896689",
        "43b462d2-e360-46e2-b6bd-73b3f5fb0721",
        "Mentor",
        2,
        "male",
        "1998-11-04T00:00:00.000Z",
        "",
        "",
        ""
    );


    System.out.println("Query: " + query);
    System.out.println("Variables: " + variable);

    Response response = GraphQlClient.execute(
        query,
        variable
    );

    return new ApiResponse<>(
        response.getStatusCode(),
        response.getHeaders(),
        response.as(AddEmployeeResponse.class)
    );
  }
}
