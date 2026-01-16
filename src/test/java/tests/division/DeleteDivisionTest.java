package tests.division;

import core.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import client.GraphQlClient;
import services.AuthService;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Map;

public class DeleteDivisionTest extends BaseTest {


    @Test
    public void testDeleteDivisionFromJson() {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Read division ID from JSON file
        String divisionId = readDivisionIdFromJson();
        if (divisionId == null) {
            Assert.fail("No division ID found in JSON file. Run DivisionDataDrivenTest first.");
        }
        
        System.out.println("=== Deleting Division from JSON Data ===");
        System.out.println("Division ID: " + divisionId);

        // Execute delete mutation
        Map<String, Object> deleteVariables = Map.of("id", divisionId);
        String deletePayload = "mutation deleteDivision($id: String!) { deleteDivision(id: $id) }";
        Response deleteResponse = GraphQlClient.execute(deletePayload, deleteVariables);

        System.out.println("Delete Response Status: " + deleteResponse.statusCode());
        System.out.println("Delete Response Body: " + deleteResponse.asString());

        Assert.assertEquals(deleteResponse.statusCode(), 200, "Delete division should return 200");
        
        String deleteResponseBody = deleteResponse.getBody().asString();
        Assert.assertTrue(deleteResponseBody.contains("\"deleteDivision\":true"), 
            "deleteDivision should return true for successful deletion");
        
        System.out.println("✓ Division deleted successfully");
        System.out.println("✓ Test completed - Division ID read from JSON and deleted");
    }

    private String readDivisionIdFromJson() {
        try {
            String filePath = "src/test/resources/employee-data/division-id.json";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String jsonContent = reader.readLine();
            reader.close();
            
            // Extract ID from JSON: {"id":"uuid-here","name":"division-name","timestamp":1234567890}
            int idStart = jsonContent.indexOf("\"id\":\"") + 6;
            int idEnd = jsonContent.indexOf("\"", idStart);
            
            if (idStart > 5 && idEnd > idStart) {
                String divisionId = jsonContent.substring(idStart, idEnd);
                System.out.println("✓ Read division ID from JSON: " + divisionId);
                return divisionId;
            }
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to read division ID from JSON: " + e.getMessage());
        }
        return null;
    }
}
