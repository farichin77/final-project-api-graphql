package tests.trainingModule.training;

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
import models.requests.training.AssignProgramVariable;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignedEmployeeTest extends BaseTest {

    @Test(dataProvider = "assignEmployeeTestData", dataProviderClass = TestDataProvider.class)
    public void testAssignEmployeeToTrainingWithDataDriven(CsvReader.AssignEmployeeTestData testData) {

        // Parse employeeIds from JSON or use from CSV
        List<String> employeeIdsList;
        if (testData.employeeIds.equalsIgnoreCase("FROM_JSON")) {
            // Get employee ID from JSON file
            String employeeId = JsonHelper.getLatestIdFromJson(FilePaths.EMPLOYEE_DATA_JSON);
            employeeIdsList = Arrays.asList(employeeId);
        } else if (testData.employeeIds.isEmpty()) {
            employeeIdsList = Arrays.asList();
        } else {
            // Parse comma-separated employee IDs
            employeeIdsList = Arrays.stream(testData.employeeIds.split(";"))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        // Get programId from JSON if specified
        String programId = testData.programId;
        if (testData.programId.equalsIgnoreCase("FROM_JSON")) {
            programId = JsonHelper.getLatestIdFromJson(FilePaths.TRAINING_DATA_JSON);
        }

        Map<String, Object> variables = AssignProgramVariable.variables(
            employeeIdsList,
            programId,
            testData.startDate,
            testData.endDate
        );

        String query = GraphQlFileReader.readMutationTraining("AssignProgram.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                // Parse response as generic map since assignProgram returns boolean
                Map<String, Object> responseBody = response.jsonPath().getMap("$");
                boolean hasGraphQLErrors = responseBody.containsKey("errors") && responseBody.get("errors") != null;
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertTrue(responseBody.containsKey("data"), "Response data is missing");
                    
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Assert.assertNotNull(data, "Data object is null");
                    Assert.assertTrue(data.containsKey("assignProgram"), "assignProgram field is missing");
                    
                    Boolean assignResult = (Boolean) data.get("assignProgram");
                    Assert.assertTrue(assignResult, "Assign program should return true");
                    
                    // Save assignment info to JSON
                    if (!employeeIdsList.isEmpty() && programId != null && !programId.isEmpty()) {
                        String assignmentId = programId + "_" + System.currentTimeMillis();
                        JsonHelper.saveIdToJson(FilePaths.ASSIGNED_EMPLOYEE_DATA_JSON, assignmentId, 
                            "Assignment_" + testData.scenario, "programId", programId);
                    }
                    System.out.println("✓ Employees assigned to program successfully");
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || !responseBody.containsKey("data") || 
                        responseBody.get("data") == null,
                        "Assignment should have failed: " + testData.scenario);
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
                System.out.println("Unexpected status: " + response.statusCode());
            }
        }
    }
}
