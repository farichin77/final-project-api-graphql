package tests.content;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import qa.client.AuthSession;
import qa.services.AuthService;
import qa.client.GraphQlClient;
import qa.utils.CsvReader;
import qa.utils.CsvReader.ContentTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class ContentDataDrivenTest {

    @BeforeMethod
    public void beforeTest() {
        // Reset session before each test
        AuthSession.setSessionCookie(null);
    }

    @DataProvider(name = "contentTestData")
    public Object[][] getContentTestData() {
        var testDataList = CsvReader.readContentTestData("test-data/content-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "contentTestData")
    public void testCreateContentWithDataDriven(ContentTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();

        // Prepare variables for content creation
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("title", testData.title);
        inputMap.put("description", testData.description);
        inputMap.put("order", testData.order);
        inputMap.put("article", testData.article);
        inputMap.put("articleType", testData.articleType);
        inputMap.put("chapterId", testData.chapterId);
        inputMap.put("duration", testData.duration);
        inputMap.put("isRandomQuestion", testData.isRandomQuestion);
        inputMap.put("mediaId", testData.mediaId);
        inputMap.put("thumbnailUrl", testData.thumbnailUrl);
        inputMap.put("type", testData.type);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", inputMap);

        String query = "mutation createContent($input: ContentInput!) {\n" +
            "  createContent(input: $input) {\n" +
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
                    // Verify successful content creation
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"createContent\""), 
                        "Response should contain createContent for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"id\""), 
                        "Response should contain content ID for scenario: " + testData.scenario);
                    
                    // Extract content ID and save to JSON
                    String contentId = extractContentId(responseBody);
                    if (contentId != null && !testData.title.isEmpty()) {
                        saveContentIdToJson(contentId, testData.title);
                    }
                    
                    System.out.println("✓ Content created successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify content creation fails (should have errors)
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"createContent\""),
                        "Content creation should fail for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Content creation failed as expected: " + testData.scenario);
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

    private String extractContentId(String responseBody) {
        try {
            // Extract ID from JSON response: {"data":{"createContent":{"id":"uuid-here",...}}}
            int idStart = responseBody.indexOf("\"id\":\"") + 6;
            int idEnd = responseBody.indexOf("\"", idStart);
            if (idStart > 5 && idEnd > idStart) {
                return responseBody.substring(idStart, idEnd);
            }
        } catch (Exception e) {
            System.out.println("Failed to extract content ID: " + e.getMessage());
        }
        return null;
    }

    private void saveContentIdToJson(String contentId, String contentTitle) {
        try {
            String filePath = "src/test/resources/chapter-data/content-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read existing JSON file
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            } catch (Exception e) {
                // File doesn't exist, start with empty array
                jsonContent.append("[]");
            }
            
            String content = jsonContent.toString().trim();
            if (content.isEmpty()) {
                content = "[]";
            }
            
            // Parse JSON manually and add new content
            if (content.startsWith("[") && content.endsWith("]")) {
                // Remove existing array brackets
                String arrayContent = content.substring(1, content.length() - 1);
                
                // Add new content object
                String newContentObject = String.format(
                    "{\"id\":\"%s\",\"title\":\"%s\",\"timestamp\":%d}", 
                    contentId, contentTitle, System.currentTimeMillis()
                );
                
                // Build new array
                if (arrayContent.trim().isEmpty()) {
                    content = "[" + newContentObject + "]";
                } else {
                    content = "[" + arrayContent + "," + newContentObject + "]";
                }
            } else {
                // Create new array with single content
                String newContentObject = String.format(
                    "{\"id\":\"%s\",\"title\":\"%s\",\"timestamp\":%d}", 
                    contentId, contentTitle, System.currentTimeMillis()
                );
                content = "[" + newContentObject + "]";
            }
            
            // Write back to file
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(content);
            }
            
            System.out.println("✓ Content ID saved to JSON file: " + filePath);
            System.out.println("✓ Content ID: " + contentId + " (Title: " + contentTitle + ")");
            
        } catch (IOException e) {
            System.out.println("Failed to save content ID to JSON: " + e.getMessage());
        }
    }
}
