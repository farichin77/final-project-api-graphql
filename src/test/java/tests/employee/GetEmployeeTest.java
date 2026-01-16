package tests.employee;

import core.BaseTest;
import io.restassured.response.Response;
import models.responses.employee.GetEmployeeResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.FilePaths;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import java.util.Map;

public class GetEmployeeTest extends BaseTest {

    @BeforeClass
    public void setup() {
        AuthService.postLogin();
    }

    @Test
    public void testGetEmployeeData() {
        String employeeId = JsonHelper.getLatestIdFromJson(FilePaths.EMPLOYEE_DATA_JSON);
        Assert.assertNotNull(employeeId, "No employee ID found in JSON");

        Map<String, Object> variables = Map.of("id", employeeId);
        String query = GraphQlFileReader.readQuery("GetEmployee.graphql");
        Response response = GraphQlClient.execute(query, variables);

        Assert.assertEquals(response.statusCode(), 200);

        GetEmployeeResponse responseBody = response.as(GetEmployeeResponse.class);
        Assert.assertNull(responseBody.errors, "Response has GraphQL errors");
        Assert.assertNotNull(responseBody.data.employeeById, "Employee data is null");
        Assert.assertEquals(responseBody.data.employeeById.id, employeeId);
    }
}
