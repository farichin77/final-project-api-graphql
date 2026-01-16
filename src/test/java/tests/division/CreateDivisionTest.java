package tests.division;

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
import models.requests.division.CreateDivisionVariable;
import models.responses.division.CreateDivisionResponse;
import io.restassured.response.Response;

import java.util.Map;

public class CreateDivisionTest extends BaseTest {

    @Test(dataProvider = "divisionTestData", dataProviderClass = TestDataProvider.class)
    public void testCreateDivisionWithDataDriven(CsvReader.DivisionTestData testData) {
        AuthService.postLogin();

        Map<String, Object> variables = CreateDivisionVariable.variables(testData.name, testData.description);
        String query = GraphQlFileReader.readMutation("CreateDivision.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                CreateDivisionResponse responseBody = response.as(CreateDivisionResponse.class);
                boolean hasGraphQLErrors = responseBody.errors != null && !responseBody.errors.isEmpty();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, "Response data is null");
                    Assert.assertNotNull(responseBody.data.createDivision, "createDivision object is null");
                    
                    String divisionId = responseBody.data.createDivision.id;
                    if (divisionId != null && !testData.name.isEmpty()) {
                        JsonHelper.saveIdToJson(FilePaths.DIVISION_DATA_JSON, divisionId, testData.name, null, null);
                    }
                    System.out.println("✓ Division created: " + divisionId);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.createDivision == null,
                        "Creation should have failed: " + testData.scenario);
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
