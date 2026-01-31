package tests.employeeModule.employee;

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
import models.requests.employee.TransferEmployeeVariable;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransferEmployee extends BaseTest {

    @Test(dataProvider = "transferEmployeeTestData", dataProviderClass = TestDataProvider.class)
    public void testTransferEmployeeWithDataDriven(CsvReader.TransferEmployeeTestData testData) {
        List<String> employeeIdsList;
        if (testData.employeeIds != null && testData.employeeIds.equalsIgnoreCase("FROM_JSON")) {
            String employeeId = JsonHelper.getLatestIdFromJson(FilePaths.EMPLOYEE_DATA_JSON);
            employeeIdsList = Arrays.asList(employeeId);
        } else if (testData.employeeIds == null || testData.employeeIds.trim().isEmpty()) {
            employeeIdsList = Arrays.asList();
        } else {
            employeeIdsList = Arrays.stream(testData.employeeIds.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        String divisionId = testData.divisionId;
        if (divisionId != null && divisionId.equalsIgnoreCase("FROM_JSON")) {
            divisionId = JsonHelper.getLatestIdFromJson(FilePaths.DIVISION_DATA_JSON);
        }

        Map<String, Object> variables = TransferEmployeeVariable.variables(employeeIdsList, divisionId);
        String query = GraphQlFileReader.readMutationEmployee("UpdateEmployeesDivision.graphql");
        Response response = GraphQlClient.execute(query, variables);

        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                Map<String, Object> responseBody = response.jsonPath().getMap("$");
                boolean hasGraphQLErrors = responseBody.containsKey("errors") && responseBody.get("errors") != null;

                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertTrue(responseBody.containsKey("data"), "Response data is missing");

                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Assert.assertNotNull(data, "Data object is null");
                    Assert.assertTrue(data.containsKey("updateEmployeesDivision"), "updateEmployeesDivision field is missing");

                    Boolean transferResult = (Boolean) data.get("updateEmployeesDivision");
                    Assert.assertTrue(transferResult, "Transfer employee should return true");

                    System.out.println("✓ Employees transferred successfully");
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || !responseBody.containsKey("data") || responseBody.get("data") == null,
                            "Transfer should have failed: " + testData.scenario);
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
            if (!("FAIL".equalsIgnoreCase(testData.expectedResult))) {
                Assert.fail("Request failed with status: " + response.statusCode());
            }
        }
    }
}
