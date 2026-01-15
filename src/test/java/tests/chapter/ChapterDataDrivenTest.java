package tests.chapter;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import qa.client.AuthSession;
import qa.services.AuthService;
import qa.client.GraphQlClient;
import qa.utils.CsvReader;
import qa.utils.CsvReader.ChapterTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class ChapterDataDrivenTest {

    @BeforeMethod
    public void beforeTest() {
        // Reset session before each test
        AuthSession.setSessionCookie(null);
    }

    @DataProvider(name = "chapterTestData")
    public Object[][] getChapterTestData() {
        var testDataList = CsvReader.readChapterTestData("test-data/chapter-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "chapterTestData")
    public void testCreateChapterWithDataDriven(ChapterTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();

        // Prepare variables for chapter creation
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("title", testData.title);
        inputMap.put("description", testData.description);
        inputMap.put("order", testData.order);
        inputMap.put("programId", testData.programId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", inputMap);

        String query = "mutation createChapter($input: ChapterInput!) {\n" +
            "  createChapter(input: $input) {\n" +
            "    id\n" +
            "    __typename\n" +
            "  }\n" +
            "}";

        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                String responseBody = response.getBody().asString();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful chapter creation
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"createChapter\""), 
                        "Response should contain createChapter for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"id\""), 
                        "Response should contain chapter ID for scenario: " + testData.scenario);
                    
                    // Extract chapter ID and save to JSON
                    String chapterId = extractChapterId(responseBody);
                    if (chapterId != null && !testData.title.isEmpty()) {
                        saveChapterIdToJson(chapterId, testData.title);
                    }
                    
                    System.out.println("✓ Chapter created successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify chapter creation fails (should have errors or no createChapter)
                    boolean hasErrors = responseBody.contains("errors");
                    boolean hasNoCreateChapter = !responseBody.contains("\"createChapter\"");
                    
                    Assert.assertTrue(hasErrors || hasNoCreateChapter,
                        "Chapter creation should fail for scenario: " + testData.scenario + 
                        " (hasErrors: " + hasErrors + ", hasNoCreateChapter: " + hasNoCreateChapter + ")");
                    
                    System.out.println("✓ Chapter creation failed as expected: " + testData.scenario);
                    if (hasErrors) {
                        System.out.println("  - Has GraphQL errors");
                    } else {
                        System.out.println("  - No createChapter in response");
                    }
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

    private String extractChapterId(String responseBody) {
        try {
            // Extract ID from JSON response: {"data":{"createChapter":{"id":"uuid-here",...}}}
            int idStart = responseBody.indexOf("\"id\":\"") + 6;
            int idEnd = responseBody.indexOf("\"", idStart);
            if (idStart > 5 && idEnd > idStart) {
                return responseBody.substring(idStart, idEnd);
            }
        } catch (Exception e) {
            System.out.println("Failed to extract chapter ID: " + e.getMessage());
        }
        return null;
    }

    private void saveChapterIdToJson(String chapterId, String chapterTitle) {
        try {
            String filePath = "src/test/resources/chapter-data/chapter-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read existing chapters if file exists
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            } catch (Exception e) {
                // File doesn't exist, start with empty array
                jsonContent.append("[]");
            }
            
            String existingJson = jsonContent.toString().trim();
            if (existingJson.isEmpty()) {
                existingJson = "[]";
            }
            
            // Parse and add new chapter
            String newChapterJson = "{\"id\":\"" + chapterId + "\",\"title\":\"" + chapterTitle + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
            
            if (existingJson.equals("[]")) {
                // First chapter
                existingJson = "[" + newChapterJson + "]";
            } else if (existingJson.startsWith("[") && existingJson.endsWith("]")) {
                // Add to existing array
                existingJson = existingJson.substring(0, existingJson.length() - 1) + "," + newChapterJson + "]";
            } else {
                // Convert single object to array and add new
                existingJson = "[" + existingJson + "," + newChapterJson + "]";
            }
            
            // Save all chapters
            FileWriter file = new FileWriter(filePath);
            file.write(existingJson);
            file.close();
            
            // Count chapters
            int chapterCount = existingJson.split("\\{\"id\"").length - 1;
            System.out.println("✓ Chapter ID saved to JSON file: " + filePath);
            System.out.println("✓ Chapter ID: " + chapterId + " (Title: " + chapterTitle + ")");
            System.out.println("✓ Total chapters saved: " + chapterCount);
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to save chapter ID to JSON: " + e.getMessage());
        }
    }
}
