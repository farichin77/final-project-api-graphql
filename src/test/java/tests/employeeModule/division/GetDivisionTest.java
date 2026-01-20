package tests.employeeModule.division;

import core.BaseTest;
import io.restassured.response.Response;
import models.responses.division.GetDivisionResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.FilePaths;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import java.util.Map;

public class GetDivisionTest extends BaseTest {

    @BeforeClass
    public void setup() {
        AuthService.postLogin();
    }

    @Test
    public void testGetDivisionData() {
        String divisionId = JsonHelper.getLatestIdFromJson(FilePaths.DIVISION_DATA_JSON);
        Assert.assertNotNull(divisionId, "No division ID found in JSON");

        Map<String, Object> variables = Map.of("id", divisionId);
        String query = GraphQlFileReader.readQuery("GetDivision.graphql");
        Response response = GraphQlClient.execute(query, variables);

        Assert.assertEquals(response.statusCode(), 200);

        GetDivisionResponse responseBody = response.as(GetDivisionResponse.class);
        Assert.assertNull(responseBody.errors, "Response has GraphQL errors");
        Assert.assertNotNull(responseBody.data.divisionById, "Division data is null");
        Assert.assertEquals(responseBody.data.divisionById.id, divisionId);
    }
}
