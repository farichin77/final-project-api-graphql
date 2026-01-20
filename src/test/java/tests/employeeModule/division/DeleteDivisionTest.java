package tests.employeeModule.division;

import core.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import client.GraphQlClient;
import services.AuthService;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import utils.FilePaths;
import models.responses.CommonDeleteResponse;

import java.util.Map;

public class DeleteDivisionTest extends BaseTest {

    @Test
    public void testDeleteDivisionFromJson() {
        AuthService.postLogin();
        
        String divisionId = JsonHelper.getLatestIdFromJson(FilePaths.DIVISION_DATA_JSON);
        if (divisionId == null) {
            Assert.fail("No division ID found in JSON file.");
        }
        
        Map<String, Object> variables = Map.of("id", divisionId);
        String query = GraphQlFileReader.readMutationEmployee("DeleteDivision.graphql");
        Response response = GraphQlClient.execute(query, variables);

        System.out.println("Division ID: " + divisionId);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                boolean isDeleted = response.jsonPath().getBoolean("data.deleteDivision");
                Assert.assertTrue(isDeleted, "deleteDivision returned false");
                System.out.println("✓ Division deleted");
            } catch (Exception e) {
                Assert.fail("Parse error: " + e.getMessage());
            }
        } else {
            Assert.fail("Request failed: " + response.statusCode());
        }
    }
}
