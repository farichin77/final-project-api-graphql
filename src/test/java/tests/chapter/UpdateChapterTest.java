package tests.chapter;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.CsvReader.UpdateChapterTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class UpdateChapterTest extends BaseTest {

    @DataProvider(name = "updateChapterTestData")
    public Object[][] getUpdateChapterTestData() {
        var testDataList = CsvReader.readUpdateChapterTestData("test-data/update-chapter-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "updateChapterTestData")
    public void testUpdateChapterWithDataDriven(UpdateChapterTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get chapter ID from JSON file
        String chapterId = getChapterIdFromJson();
        if (chapterId == null) {
            Assert.fail("No chapter ID found in JSON file. Run ChapterDataDrivenTest first.");
        }
        
        // Replace placeholder with actual chapter ID
        String actualId = testData.id.equals("{lastCreatedId}") ? chapterId : testData.id;
        
        System.out.println("=== Updating Chapter ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Chapter ID: " + actualId);
        System.out.println("New Title: " + testData.title);
        System.out.println("New Description: " + testData.description);
        System.out.println("New Order: " + testData.order);

        // Prepare variables for chapter update
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("title", testData.title);
        inputMap.put("description", testData.description);
        inputMap.put("order", testData.order);
        inputMap.put("programId", testData.programId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", actualId);
        variables.put("input", inputMap);

        String query = "mutation updateChapter($id: String!, $input: ChapterInput!) {\n" +
            "  updateChapter(id: $id, input: $input) {\n" +
            "    id\n" +
            "    __typename\n" +
            "  }\n" +
            "}";

        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Update Response Status: " + response.statusCode());
        System.out.println("Update Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                String responseBody = response.getBody().asString();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful chapter update
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"updateChapter\""), 
                        "Response should contain updateChapter for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"id\""), 
                        "Response should contain chapter ID for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Chapter updated successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify chapter update fails (should have errors)
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"updateChapter\""),
                        "Chapter update should fail for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Chapter update failed as expected: " + testData.scenario);
                }
            } catch (Exception e) {
                if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    System.out.println("Expected failure for: " + testData.scenario + " - " + e.getMessage());
                } else {
                    throw e;
                }
            }
        } else {
            // If status code is not 200, the request failed
            if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                System.out.println("Test expected to fail and it did with status: " + response.statusCode());
            } else {
                System.out.println("Scenario: " + testData.scenario + " - Status: " + response.statusCode());
            }
        }
    }

    private String getChapterIdFromJson() {
        try {
            String filePath = "src/test/resources/chapter-data/chapter-id.json";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String jsonContent = reader.readLine();
            reader.close();
            
            if (jsonContent != null && !jsonContent.trim().isEmpty()) {
                if (jsonContent.startsWith("[")) {
                    // Array format: [{"id":"uuid1",...},{"id":"uuid2",...}]
                    String arrayContent = jsonContent.substring(1, jsonContent.length() - 1);
                    String[] chapterObjects = arrayContent.split("\\},\\{");
                    
                    // Get last chapter ID from JSON array
                    String lastChapterId = null;
                    if (chapterObjects.length > 0) {
                        // Get last chapter object
                        String lastChapterObj = chapterObjects[chapterObjects.length - 1];
                        if (!lastChapterObj.startsWith("{")) {
                            lastChapterObj = "{" + lastChapterObj;
                        }
                        if (!lastChapterObj.endsWith("}")) {
                            lastChapterObj = lastChapterObj + "}";
                        }
                        
                        // Extract ID from last chapter
                        int idStart = lastChapterObj.indexOf("\"id\":\"");
                        if (idStart != -1) {
                            idStart += 6; // length of "id":" 
                            int idEnd = lastChapterObj.indexOf("\"", idStart);
                            if (idEnd != -1) {
                                lastChapterId = lastChapterObj.substring(idStart, idEnd);
                            }
                        }
                    }
                    
                    if (lastChapterId != null) {
                        System.out.println("✓ Read last chapter ID from JSON: " + lastChapterId);
                        return lastChapterId;
                    }
                } else if (jsonContent.startsWith("{")) {
                    // Single object format: {"id":"uuid",...}
                    int idStart = jsonContent.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6; // length of "id":"
                        int idEnd = jsonContent.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String chapterId = jsonContent.substring(idStart, idEnd);
                            System.out.println("✓ Read chapter ID from JSON: " + chapterId);
                            return chapterId;
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to read chapter ID from JSON: " + e.getMessage());
        }
        return null;
    }
}
