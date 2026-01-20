package tests.employeeModule.employee;

import core.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import client.GraphQlClient;
import models.requests.employee.DeleteEmployeeVariable;
import models.responses.employee.DeleteEmployeeResponse;
import services.AuthService;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import utils.FilePaths;

import java.util.Map;

public class DeleteEmployeeTest extends BaseTest {

    @Test
    public void testDeleteEmployeeFromJson() {
        AuthService.postLogin();
        
        String employeeId = JsonHelper.getLatestIdFromJson(FilePaths.EMPLOYEE_DATA_JSON);
        if (employeeId == null) {
            Assert.fail("No employee ID found in JSON file.");
        }
        
        Map<String, Object> deleteVariables = DeleteEmployeeVariable.variables(employeeId);
        String deletePayload = GraphQlFileReader.readMutationEmployee("DeleteEmployee.graphql");
        Response response = GraphQlClient.execute(deletePayload, deleteVariables);

        System.out.println("Employee ID: " + employeeId);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                DeleteEmployeeResponse responseBody = response.as(DeleteEmployeeResponse.class);
                boolean isDeleted = responseBody.data != null && Boolean.TRUE.equals(responseBody.data.deleteEmployee);
                
                if (isDeleted) {
                    System.out.println("✓ Employee deleted");
                } else {
                    Assert.fail("Deletion failed");
                }
            } catch (Exception e) {
                Assert.fail("Parse error: " + e.getMessage());
            }
        } else {
            Assert.fail("Request failed: " + response.statusCode());
        }
    }
}
