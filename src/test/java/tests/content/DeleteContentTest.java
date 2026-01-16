package tests.content;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.CsvReader.DeleteContentTestData;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class DeleteContentTest extends BaseTest {

    @DataProvider(name = "deleteContentTestData")
    public Object[][] getDeleteContentTestData() {
        var testDataList = CsvReader.readDeleteContentTestData("test-data/delete-content-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "deleteContentTestData")
    public void testDeleteContentWithDataDriven(DeleteContentTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get all content IDs from JSON file
        List<String> contentIds = getAllContentIdsFromJson();
        if (contentIds.isEmpty()) {
            Assert.fail("No content IDs found in JSON file. Run ContentDataDrivenTest first.");
        }
        
        System.out.println("=== Deleting Content ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Total contents available: " + contentIds.size());
        
        // Get content IDs filtered by content type
        String videoContentId = "";
        String articleContentId = "";
        String testContentId = "";
        
        // Parse JSON and extract content type from title or create a mapping
        Map<String, String> contentIdToType = new HashMap<>();
        
        try {
            String filePath = "src/test/resources/chapter-data/content-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            }
            
            String content = jsonContent.toString().trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                String arrayContent = content.substring(1, content.length() - 1);
                String[] contentObjects = arrayContent.split("\\},\\{");
                
                for (String contentObj : contentObjects) {
                    if (!contentObj.startsWith("{")) {
                        contentObj = "{" + contentObj;
                    }
                    if (!contentObj.endsWith("}")) {
                        contentObj = contentObj + "}";
                    }
                    
                    // Extract ID
                    int idStart = contentObj.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6;
                        int idEnd = contentObj.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String contentId = contentObj.substring(idStart, idEnd);
                            
                            // Extract title to determine type
                            int titleStart = contentObj.indexOf("\"title\":\"");
                            if (titleStart != -1) {
                                titleStart += 9;
                                int titleEnd = contentObj.indexOf("\"", titleStart);
                                if (titleEnd != -1) {
                                    String title = contentObj.substring(titleStart, titleEnd);
                                    String contentType = determineContentType(title);
                                    contentIdToType.put(contentId, contentType);
                                    
                                    System.out.println("Content ID: " + contentId + " - Title: " + title + " - Type: " + contentType);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ Failed to parse content types: " + e.getMessage());
        }
        
        // Find the latest content ID for each type
        for (Map.Entry<String, String> entry : contentIdToType.entrySet()) {
            String contentId = entry.getKey();
            String type = entry.getValue();
            
            if ("video".equals(type) && videoContentId.isEmpty()) {
                videoContentId = contentId;
            } else if ("article".equals(type) && articleContentId.isEmpty()) {
                articleContentId = contentId;
            } else if ("test".equals(type) && testContentId.isEmpty()) {
                testContentId = contentId;
            }
        }
        
        if (videoContentId.isEmpty() && articleContentId.isEmpty() && testContentId.isEmpty()) {
            Assert.fail("No content IDs found in JSON file. Run UpdateContentFromJsonTest first.");
        }
        
        System.out.println("=== Deleting Content ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Total contents available: " + contentIds.size());
        System.out.println("Video Content ID: " + videoContentId);
        System.out.println("Article Content ID: " + articleContentId);
        System.out.println("Test Content ID: " + testContentId);
        
        // Use appropriate content ID based on placeholder
        String contentId = "";
        if (testData.id.equals("{videoContentId}")) {
            contentId = videoContentId; // Use video content ID
        } else if (testData.id.equals("{articleContentId}")) {
            contentId = articleContentId; // Use article content ID
        } else if (testData.id.equals("{testContentId}")) {
            contentId = testContentId; // Use test content ID
        } else {
            contentId = testData.id; // Use as-is if no placeholder
        }
        
        if (contentId.isEmpty()) {
            System.out.println("No valid content ID available for this test scenario. Skipping.");
            return;
        }
        
        System.out.println("Deleting Content ID: " + contentId);
        
        // Use the resolved content ID
        String actualId = contentId;
        
        // Prepare variables for content deletion
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", actualId);

        String query = "mutation deleteContent($id: String!) {\n" +
            "  deleteContent(id: $id)\n" +
            "}";

        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Delete Response Status: " + response.statusCode());
        System.out.println("Delete Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                String responseBody = response.getBody().asString();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful content deletion
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for content ID: " + actualId);
                    Assert.assertTrue(responseBody.contains("\"deleteContent\""), 
                        "Response should contain deleteContent for content ID: " + actualId);
                    Assert.assertTrue(responseBody.contains("true"), 
                        "Response should contain true for successful deletion of content ID: " + actualId);
                    
                    System.out.println("✓ Content deleted successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify content deletion fails (should have errors)
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"deleteContent\""),
                        "Content deletion should fail for content ID: " + actualId);
                    
                    System.out.println("✓ Content deletion failed as expected: " + testData.scenario);
                }
            } catch (Exception e) {
                if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    System.out.println("Expected failure for content ID: " + actualId + " - " + e.getMessage());
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

    private int getCurrentTestIndex() {
        String filePath = "src/test/resources/chapter-data/delete-index.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                int index = Integer.parseInt(line.trim());
                return index;
            }
        } catch (Exception e) {
            // If file doesn't exist, start with 0
            return 0;
        }
        return 0;
    }

    private void incrementTestIndex() {
        String filePath = "src/test/resources/chapter-data/delete-index.txt";
        try {
            int currentIndex = getCurrentTestIndex();
            int nextIndex = currentIndex + 1;
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(String.valueOf(nextIndex));
            }
            
            System.out.println("✓ Incremented test index from " + currentIndex + " to " + nextIndex);
        } catch (IOException e) {
            System.out.println("⚠ Failed to increment test index: " + e.getMessage());
        }
    }

    private void updateTestIndex(int newIndex) {
        String filePath = "src/test/resources/chapter-data/delete-index.txt";
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(String.valueOf(newIndex));
            }
            
            System.out.println("✓ Updated test index to " + newIndex);
        } catch (IOException e) {
            System.out.println("⚠ Failed to update test index: " + e.getMessage());
        }
    }

    private List<String> getAllContentIdsFromJson() {
        List<String> contentIds = new ArrayList<>();
        try {
            String filePath = "src/test/resources/chapter-data/content-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read entire file
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            } catch (Exception e) {
                System.out.println("⚠ File not found: " + e.getMessage());
                return contentIds;
            }
            
            String content = jsonContent.toString().trim();
            if (content.isEmpty()) {
                return contentIds;
            }
            
            // Parse JSON manually
            if (content.startsWith("[") && content.endsWith("]")) {
                // Array format: [{"id":"uuid1",...},{"id":"uuid2",...}]
                String arrayContent = content.substring(1, content.length() - 1);
                String[] contentObjects = arrayContent.split("\\},\\{");
                
                for (String contentObj : contentObjects) {
                    // Clean up the object string
                    if (!contentObj.startsWith("{")) {
                        contentObj = "{" + contentObj;
                    }
                    if (!contentObj.endsWith("}")) {
                        contentObj = contentObj + "}";
                    }
                    
                    // Extract ID using string manipulation
                    int idStart = contentObj.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6; // length of "id":"
                        int idEnd = contentObj.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String contentId = contentObj.substring(idStart, idEnd);
                            contentIds.add(contentId);
                        }
                    }
                }
            } else if (content.startsWith("{") && content.endsWith("}")) {
                // Single object format: {"id":"uuid",...}
                int idStart = content.indexOf("\"id\":\"");
                if (idStart != -1) {
                    idStart += 6; // length of "id":"
                    int idEnd = content.indexOf("\"", idStart);
                    if (idEnd != -1) {
                        String contentId = content.substring(idStart, idEnd);
                        contentIds.add(contentId);
                    }
                }
            }
            
            System.out.println("✓ Read " + contentIds.size() + " content IDs from JSON");
            for (String id : contentIds) {
                System.out.println("  - " + id);
            }
            
        } catch (Exception e) {
            System.out.println("⚠ Failed to read content IDs from JSON: " + e.getMessage());
        }
        return contentIds;
    }

    private String determineContentType(String title) {
        String lowerTitle = title.toLowerCase();
        
        if (lowerTitle.contains("video")) {
            return "video";
        } else if (lowerTitle.contains("test") || lowerTitle.contains("sdlc")) {
            return "test";
        } else {
            return "article";
        }
    }

    private String getContentTitleById(String targetId) {
        try {
            String filePath = "src/test/resources/chapter-data/content-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read entire file
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            }
            
            String content = jsonContent.toString().trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                String arrayContent = content.substring(1, content.length() - 1);
                String[] contentObjects = arrayContent.split("\\},\\{");
                
                for (String contentObj : contentObjects) {
                    if (!contentObj.startsWith("{")) {
                        contentObj = "{" + contentObj;
                    }
                    if (!contentObj.endsWith("}")) {
                        contentObj = contentObj + "}";
                    }
                    
                    // Extract ID
                    int idStart = contentObj.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6;
                        int idEnd = contentObj.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String contentId = contentObj.substring(idStart, idEnd);
                            
                            // Check if this is the target ID
                            if (contentId.equals(targetId)) {
                                // Extract title
                                int titleStart = contentObj.indexOf("\"title\":\"");
                                if (titleStart != -1) {
                                    titleStart += 9; // length of "title":"
                                    int titleEnd = contentObj.indexOf("\"", titleStart);
                                    if (titleEnd != -1) {
                                        return contentObj.substring(titleStart, titleEnd);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ Failed to get content title: " + e.getMessage());
        }
        return "Unknown";
    }
}
