package tests.employee;

import core.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import client.GraphQlClient;
import models.requests.employee.DeleteEmployeeVariable;
import models.responses.employee.DeleteEmployeeResponse;
import services.AuthService;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Map;

public class DeleteEmployeeTest extends BaseTest {


    @Test
    public void testDeleteEmployeeFromJson() {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Read employee ID from file
        String employeeId = readEmployeeIdFromFile();
        if (employeeId == null) {
            Assert.fail("No employee ID found in file. Run EmployeeDataDrivenTest first.");
        }
        
        System.out.println("=== Deleting Employee from File Data ===");
        System.out.println("Employee ID: " + employeeId);

        // Execute delete mutation
        Map<String, Object> deleteVariables = DeleteEmployeeVariable.variables(employeeId);
        String deletePayload = "mutation deleteEmployee($id: String!) { deleteEmployee(id: $id) }";
        Response deleteResponse = GraphQlClient.execute(deletePayload, deleteVariables);

        System.out.println("Delete Response Status: " + deleteResponse.statusCode());
        System.out.println("Delete Response Body: " + deleteResponse.asString());

        Assert.assertEquals(deleteResponse.statusCode(), 200, "Delete employee should return 200");
        
        DeleteEmployeeResponse deleteResponseBody = deleteResponse.as(DeleteEmployeeResponse.class);
        Assert.assertTrue(deleteResponseBody.data.deleteEmployee, 
            "deleteEmployee should return true for successful deletion");
        
        System.out.println("✓ Employee deleted successfully");
        System.out.println("✓ Test completed - Employee ID read from file and deleted");
    }

    private String readEmployeeIdFromFile() {
        try {
            String filePath = "src/test/resources/employee-data/employee-id.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String employeeId = reader.readLine();
            reader.close();
            
            System.out.println("✓ Read employee ID from file: " + employeeId);
            return employeeId;
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to read employee ID from file: " + e.getMessage());
            return null;
        }
    }
}
