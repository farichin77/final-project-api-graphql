package tests.employeeModule.division;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.TestDataProvider;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import utils.FilePaths;
import models.requests.division.UpdateDivisionVariable;
import models.responses.division.UpdateDivisionResponse;
import io.restassured.response.Response;

import java.util.Map;

public class UpdateDivisionTest extends BaseTest {

    @Test(dataProvider = "updateDivisionTestData", dataProviderClass = TestDataProvider.class)
    public void testUpdateDivisionWithDataDriven(CsvReader.UpdateDivisionTestData testData) {
        AuthService.postLogin();
        
        String divisionId = JsonHelper.getLatestIdFromJson(FilePaths.DIVISION_DATA_JSON);
        if (divisionId == null) {
            Assert.fail("No division ID found in JSON file.");
        }
        
        String actualId = "{lastCreatedId}".equals(testData.id) ? divisionId : testData.id;
        
        Map<String, Object> variables = UpdateDivisionVariable.variables(actualId, testData.name, testData.description);
        String query = GraphQlFileReader.readMutationEmployee("UpdateDivision.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                UpdateDivisionResponse responseBody = response.as(UpdateDivisionResponse.class);
                boolean hasGraphQLErrors = responseBody.errors != null && !responseBody.errors.isEmpty();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, "Response data is null");
                    Assert.assertNotNull(responseBody.data.updateDivision, "updateDivision object is null");
                    Assert.assertEquals(responseBody.data.updateDivision.id, actualId, "ID mismatch");
                    
                    System.out.println("✓ Division updated: " + actualId);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.updateDivision == null,
                        "Update should have failed: " + testData.scenario);
                    System.out.println("✓ Failure confirmed as expected");
                }
            } catch (Exception e) {
                if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    System.out.println("Expected failure: " + e.getMessage());
                } else {
                    throw e;
                }
            }
        } else {
            if (!"FAIL".equalsIgnoreCase(testData.expectedResult)) {
                Assert.fail("Request failed with status: " + response.statusCode());
            }
        }
    }
}
