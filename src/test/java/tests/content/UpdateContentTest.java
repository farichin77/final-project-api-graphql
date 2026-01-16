package tests.content;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.CsvReader.UpdateContentTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class UpdateContentTest extends BaseTest {

    @DataProvider(name = "updateContentTestData")
    public Object[][] getUpdateContentTestData() {
        var testDataList = CsvReader.readUpdateContentTestData("test-data/update-content-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "updateContentTestData")
    public void testUpdateContentWithDataDriven(UpdateContentTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get content ID from JSON file
        String contentId = getContentIdFromJson();
        if (contentId == null) {
            Assert.fail("No content ID found in JSON file. Run ContentDataDrivenTest first.");
        }
        
        // Replace placeholder with actual content ID
        String actualId = testData.id.equals("{lastCreatedId}") ? contentId : testData.id;
        
        System.out.println("=== Updating Content ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Content ID: " + actualId);
        System.out.println("New Title: " + testData.title);
        System.out.println("New Description: " + testData.description);
        System.out.println("New Order: " + testData.order);

        // Prepare variables for content update
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
        variables.put("id", actualId);
        variables.put("input", inputMap);

        String query = "mutation updateContent($id: String!, $input: ContentInput!) {\n" +
            "  updateContent(id: $id, input: $input) {\n" +
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
                    // Verify successful content update
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"updateContent\""), 
                        "Response should contain updateContent for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"id\""), 
                        "Response should contain content ID for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Content updated successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify content update fails (should have errors)
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"updateContent\""),
                        "Content update should fail for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Content update failed as expected: " + testData.scenario);
                }
            } catch (Exception e) {
                if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    System.out.println("Expected failure for: " + testData.scenario + " - " + e.getMessage());
                } else {
                    throw e;
                }
            }
        } else {
            // If status code is not 200, request failed
            if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                System.out.println("Test expected to fail and it did with status: " + response.statusCode());
            } else {
                System.out.println("Scenario: " + testData.scenario + " - Status: " + response.statusCode());
            }
        }
    }

    private String getContentIdFromJson() {
        try {
            String filePath = "src/test/resources/chapter-data/content-id.json";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String jsonContent = reader.readLine();
            reader.close();
            
            if (jsonContent != null && !jsonContent.trim().isEmpty()) {
                if (jsonContent.startsWith("[")) {
                    // Array format: [{"id":"uuid1",...},{"id":"uuid2",...}]
                    String arrayContent = jsonContent.substring(1, jsonContent.length() - 1);
                    String[] contentObjects = arrayContent.split("\\},\\{");
                    
                    // Get last content ID from JSON array
                    String lastContentId = null;
                    if (contentObjects.length > 0) {
                        // Get last content object
                        String lastContentObj = contentObjects[contentObjects.length - 1];
                        if (!lastContentObj.startsWith("{")) {
                            lastContentObj = "{" + lastContentObj;
                        }
                        if (!lastContentObj.endsWith("}")) {
                            lastContentObj = lastContentObj + "}";
                        }
                        
                        // Extract ID from last content
                        int idStart = lastContentObj.indexOf("\"id\":\"");
                        if (idStart != -1) {
                            idStart += 6; // length of "id":"
                            int idEnd = lastContentObj.indexOf("\"", idStart);
                            if (idEnd != -1) {
                                lastContentId = lastContentObj.substring(idStart, idEnd);
                            }
                        }
                    }
                    
                    if (lastContentId != null) {
                        System.out.println("✓ Read last content ID from JSON: " + lastContentId);
                        return lastContentId;
                    }
                } else if (jsonContent.startsWith("{")) {
                    // Single object format: {"id":"uuid",...}
                    int idStart = jsonContent.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6; // length of "id":"
                        int idEnd = jsonContent.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String contentId = jsonContent.substring(idStart, idEnd);
                            System.out.println("✓ Read content ID from JSON: " + contentId);
                            return contentId;
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to read content ID from JSON: " + e.getMessage());
        }
        return null;
    }
}
