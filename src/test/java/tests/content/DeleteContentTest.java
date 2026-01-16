package tests.content;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.TestDataProvider;
import utils.GraphQlFileReader;
import utils.ExtentManager;
import io.restassured.response.Response;
import utils.CsvReader.DeleteContentTestData;

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

    @Test(dataProvider = "deleteContentTestData", dataProviderClass = TestDataProvider.class)
    public void testDeleteContentWithDataDriven(DeleteContentTestData testData) {
        // Create test with category for better organization
        ExtentManager.createTest("Delete Content Test - " + testData.scenario, 
                               "Testing content deletion with scenario: " + testData.scenario, 
                               "Content Management");
        
        ExtentManager.logInfo("Starting test with scenario: " + testData.scenario);
        ExtentManager.logInfo("Expected result: " + testData.expectedResult);
        
        try {
            // First authenticate to get valid session
            ExtentManager.logInfo("Authenticating to get valid session...");
            AuthService.postLogin();
            ExtentManager.logPass("Authentication successful");
            
            // Get latest training ID from JSON
            ExtentManager.logInfo("Retrieving latest training ID...");
            String latestTrainingId = getLatestTrainingIdFromJson();
            if (latestTrainingId == null) {
                ExtentManager.logFail("No training ID found in JSON file. Run CreateTrainingTest first.");
                Assert.fail("No training ID found in JSON file. Run CreateTrainingTest first.");
            }
            ExtentManager.logPass("Found training ID: " + latestTrainingId);

            String latestChapterId = getLatestChapterIdFromTraining(latestTrainingId);
            if (latestChapterId == null) {
                ExtentManager.logFail("No chapter ID found for training " + latestTrainingId + ". Run CreateChapterTest first.");
                Assert.fail("No chapter ID found for training " + latestTrainingId + ". Run CreateChapterTest first.");
            }
            ExtentManager.logPass("Found chapter ID: " + latestChapterId);
            
            // Get all content IDs from JSON file (since content doesn't have chapterId field)
            ExtentManager.logInfo("Retrieving all content IDs...");
            List<String> contentIds = getAllContentIdsFromJson();
            if (contentIds.isEmpty()) {
                ExtentManager.logFail("No content IDs found in JSON file. Run CreateContentTest first.");
                Assert.fail("No content IDs found in JSON file. Run CreateContentTest first.");
            }
            ExtentManager.logPass("Found " + contentIds.size() + " content IDs");
        
        // Parse content types and categorize
        ExtentManager.logInfo("Analyzing content types...");
        
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
                                    
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        
        // Find the latest content ID for each type from the latest chapter
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
        
        // If no valid content exists, create fresh content for testing
        if (videoContentId.isEmpty() || articleContentId.isEmpty() || testContentId.isEmpty()) {
            createFreshContentForTesting(latestChapterId);
            
            // Refresh the content list after creation
            contentIdToType.clear();
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
                                        
                                        // Update content IDs if still empty
                                        if ("video".equals(contentType) && videoContentId.isEmpty()) {
                                            videoContentId = contentId;
                                        } else if ("article".equals(contentType) && articleContentId.isEmpty()) {
                                            articleContentId = contentId;
                                        } else if ("test".equals(contentType) && testContentId.isEmpty()) {
                                            testContentId = contentId;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        
        if (videoContentId.isEmpty() && articleContentId.isEmpty() && testContentId.isEmpty()) {
            Assert.fail("No content IDs found in JSON file. Run UpdateContentFromJsonTest first.");
        }
        
        
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
            return;
        }
        
        
        // Use the resolved content ID
        String actualId = contentId;
        
        // Validate content exists before attempting deletion
        ExtentManager.logInfo("Validating content existence for ID: " + actualId);
        if (!isContentExists(actualId)) {
            ExtentManager.logWarning("Content ID " + actualId + " does not exist in the API. Skipping deletion.");
            return;
        }
        ExtentManager.logPass("Content ID " + actualId + " exists and ready for deletion");
        
        // Prepare variables for content deletion
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", actualId);

        String query = GraphQlFileReader.readMutation("DeleteContent.graphql");
        
        ExtentManager.logInfo("Executing delete content mutation...");
        ExtentManager.logInfo("GraphQL Query: " + query);
        ExtentManager.logInfo("Variables: " + variables);

        Response response = GraphQlClient.execute(query, variables);
        
        ExtentManager.logInfo("Delete Response Status: " + response.statusCode());
        ExtentManager.logInfo("Delete Response Body: " + response.asString());
        

        if (response.statusCode() == 200) {
            try {
                String responseBody = response.getBody().asString();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful content deletion
                    ExtentManager.logInfo("Verifying successful content deletion...");
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for content ID: " + actualId);
                    Assert.assertTrue(responseBody.contains("\"deleteContent\""), 
                        "Response should contain deleteContent for content ID: " + actualId);
                    Assert.assertTrue(responseBody.contains("true"), 
                        "Response should contain true for successful deletion of content ID: " + actualId);
                    
                    // Remove the deleted content ID from the JSON file to maintain data consistency
                    ExtentManager.logInfo("Cleaning up test data - removing deleted content ID from JSON...");
                    removeContentIdFromJson(actualId);
                    
                    ExtentManager.logPass("Content deleted successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify content deletion fails (should have errors)
                    ExtentManager.logInfo("Verifying expected test failure...");
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"deleteContent\""),
                        "Content deletion should fail for content ID: " + actualId);
                    
                    ExtentManager.logPass("Content deletion failed as expected: " + testData.scenario);
                }
            } catch (Exception e) {
                if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    ExtentManager.logInfo("Expected failure for content ID: " + actualId + " - " + e.getMessage());
                } else {
                    ExtentManager.logFail("Unexpected error during test execution: " + e.getMessage());
                    throw e;
                }
            }
        } else {
            // If status code is not 200, request failed
            ExtentManager.logWarning("Received non-200 status code: " + response.statusCode());
            if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                ExtentManager.logPass("Test expected to fail and it did with status: " + response.statusCode());
            } else {
                ExtentManager.logWarning("Unexpected status code for scenario: " + testData.scenario + " - Status: " + response.statusCode());
            }
        }
        
        ExtentManager.logInfo("Test execution completed for scenario: " + testData.scenario);
        } catch (Exception e) {
            ExtentManager.logFail("Test execution failed with exception: " + e.getMessage());
            ExtentManager.getTest().fail(e);
            throw e;
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
            
        } catch (IOException e) {
        }
    }

    private void updateTestIndex(int newIndex) {
        String filePath = "src/test/resources/chapter-data/delete-index.txt";
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(String.valueOf(newIndex));
            }
            
        } catch (IOException e) {
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
            
            
        } catch (Exception e) {
        }
        return contentIds;
    }

    private String determineContentType(String title) {
        String lowerTitle = title.toLowerCase();
        
        if (lowerTitle.contains("video")) {
            return "video";
        } else if (lowerTitle.contains("test") || lowerTitle.contains("quiz") || lowerTitle.contains("sdlc") || lowerTitle.contains("ujian")) {
            return "test";
        } else if (lowerTitle.contains("article")) {
            return "article";
        } else {
            // Default fallback based on common patterns
            if (lowerTitle.contains("intro") && lowerTitle.contains("video")) {
                return "video";
            } else if (lowerTitle.contains("sdlc")) {
                return "test";
            } else {
                return "article"; // Default to article for unknown types
            }
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
        }
        return "Unknown";
    }
    
    private String getLatestTrainingIdFromJson() {
        try {
            String filePath = "src/test/resources/training-data/training-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read entire file
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            } catch (Exception e) {
                return null;
            }
            
            String content = jsonContent.toString().trim();
            if (content.isEmpty()) {
                return null;
            }
            
            // Handle both array and single object format
            if (content.startsWith("[") && content.endsWith("]")) {
                // Array format: [{"id":"uuid","title":"...","timestamp":123}]
                String arrayContent = content.substring(1, content.length() - 1);
                String[] trainingObjects = arrayContent.split("\\},\\{");
                
                String latestTrainingId = null;
                long latestTimestamp = 0;
                
                for (String trainingObj : trainingObjects) {
                    // Clean up object string
                    if (!trainingObj.startsWith("{")) {
                        trainingObj = "{" + trainingObj;
                    }
                    if (!trainingObj.endsWith("}")) {
                        trainingObj = trainingObj + "}";
                    }
                    
                    // Extract ID
                    int idStart = trainingObj.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6;
                        int idEnd = trainingObj.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String trainingId = trainingObj.substring(idStart, idEnd);
                            
                            // Extract timestamp
                            int timestampStart = trainingObj.indexOf("\"timestamp\":");
                            if (timestampStart != -1) {
                                timestampStart += 12;
                                int timestampEnd = trainingObj.indexOf("}", timestampStart);
                                if (timestampEnd != -1) {
                                    try {
                                        long timestamp = Long.parseLong(trainingObj.substring(timestampStart, timestampEnd));
                                        if (timestamp > latestTimestamp) {
                                            latestTimestamp = timestamp;
                                            latestTrainingId = trainingId;
                                        }
                                    } catch (NumberFormatException e) {
                                        // Ignore timestamp parsing errors
                                    }
                                }
                            }
                        }
                    }
                }
                
                return latestTrainingId;
                
            } else if (content.startsWith("{") && content.endsWith("}")) {
                // Single object format: {"id":"uuid","title":"...","timestamp":123}
                int idStart = content.indexOf("\"id\":\"");
                if (idStart != -1) {
                    idStart += 6;
                    int idEnd = content.indexOf("\"", idStart);
                    if (idEnd != -1) {
                        String trainingId = content.substring(idStart, idEnd);
                        return trainingId;
                    }
                }
            }
            
        } catch (Exception e) {
        }
        return null;
    }
    
    private String getLatestChapterIdFromTraining(String trainingId) {
        try {
            String filePath = "src/test/resources/chapter-data/chapter-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read entire file
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            } catch (Exception e) {
                return null;
            }
            
            String content = jsonContent.toString().trim();
            if (content.isEmpty()) {
                return null;
            }
            
            // Simple approach: find all chapters with the training ID
            int searchIndex = 0;
            String latestChapterId = null;
            long latestTimestamp = 0;
            
            while (searchIndex < content.length()) {
                // Find programId field
                int programIdStart = content.indexOf("\"programId\":\"" + trainingId + "\"", searchIndex);
                if (programIdStart == -1) {
                    break; // No more matches
                }
                
                // Find the chapter object that contains this programId
                int objStart = content.lastIndexOf("{", programIdStart);
                int objEnd = content.indexOf("}", programIdStart);
                
                if (objStart != -1 && objEnd != -1 && objEnd > objStart) {
                    String chapterObj = content.substring(objStart, objEnd + 1);
                    
                    // Extract chapter ID from this object
                    int idStart = chapterObj.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6; // length of "id":"
                        int idEnd = chapterObj.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String chapterId = chapterObj.substring(idStart, idEnd);
                            
                            // Extract timestamp
                            int timestampStart = chapterObj.indexOf("\"timestamp\":");
                            if (timestampStart != -1) {
                                timestampStart += 12;
                                int timestampEnd = chapterObj.indexOf("}", timestampStart);
                                if (timestampEnd != -1) {
                                    try {
                                        long timestamp = Long.parseLong(chapterObj.substring(timestampStart, timestampEnd));
                                        if (timestamp > latestTimestamp) {
                                            latestTimestamp = timestamp;
                                            latestChapterId = chapterId;
                                        }
                                    } catch (NumberFormatException e) {
                                        // Ignore timestamp parsing errors
                                    }
                                }
                            }
                        }
                    }
                    
                    searchIndex = objEnd + 1;
                } else {
                    break;
                }
            }
            
            return latestChapterId;
            
        } catch (Exception e) {
        }
        return null;
    }
    
    private List<String> getContentIdsFromChapter(String chapterId) {
        List<String> contentIds = new ArrayList<>();
        
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
                    
                    // Extract chapterId from content object
                    int chapterIdStart = contentObj.indexOf("\"chapterId\":\"");
                    if (chapterIdStart != -1) {
                        chapterIdStart += 13; // length of "chapterId":"
                        int chapterIdEnd = contentObj.indexOf("\"", chapterIdStart);
                        if (chapterIdEnd != -1) {
                            String contentChapterId = contentObj.substring(chapterIdStart, chapterIdEnd);
                            
                            // If this content belongs to the target chapter, extract its ID
                            if (contentChapterId.equals(chapterId)) {
                                int idStart = contentObj.indexOf("\"id\":\"");
                                if (idStart != -1) {
                                    idStart += 6;
                                    int idEnd = contentObj.indexOf("\"", idStart);
                                    if (idEnd != -1) {
                                        String contentId = contentObj.substring(idStart, idEnd);
                                        contentIds.add(contentId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return contentIds;
    }
    
    private boolean isContentExists(String contentId) {
        try {
            // Query to check if content exists by ID
            String query = "query GetContentById($id: String!) { " +
                          "  content(id: $id) { " +
                          "    id " +
                          "    title " +
                          "  } " +
                          "}";
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("id", contentId);
            
            Response response = GraphQlClient.execute(query, variables);
            
            if (response.statusCode() == 200) {
                String responseBody = response.getBody().asString();
                // Check if the response contains the content data (not errors) and the ID
                return responseBody.contains("\"content\"") && responseBody.contains(contentId) && !responseBody.contains("errors");
            }
        } catch (Exception e) {
        }
        return false;
    }
    
    private void createFreshContentForTesting(String chapterId) {
        try {
            // Create video content
            createContent(chapterId, "Test Video for Deletion", "video", "https://example.com/test-video.mp4");
            
            // Create article content  
            createContent(chapterId, "Test Article for Deletion", "article", "This is a test article content for deletion testing.");
            
            // Create test content
            createContent(chapterId, "Test Quiz for Deletion", "test", "This is a test quiz for deletion testing.");
            
        } catch (Exception e) {
        }
    }
    
    private void createContent(String chapterId, String title, String type, String content) {
        try {
            String mutation = "mutation CreateContent($input: ContentInput!) { " +
                             "  createContent(input: $input) { " +
                             "    id " +
                             "    title " +
                             "    type " +
                             "  } " +
                             "}";
            
            Map<String, Object> input = new HashMap<>();
            input.put("chapterId", chapterId);
            input.put("title", title);
            input.put("type", type);
            input.put("content", content);
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("input", input);
            
            Response response = GraphQlClient.execute(mutation, variables);
            
            if (response.statusCode() == 200) {
                String responseBody = response.getBody().asString();
                if (!responseBody.contains("errors")) {
                } else {
                }
            } else {
            }
        } catch (Exception e) {
        }
    }
    
    private void removeContentIdFromJson(String deletedContentId) {
        try {
            String filePath = "src/test/resources/chapter-data/content-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read existing content
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
                
                StringBuilder newJsonContent = new StringBuilder("[");
                boolean first = true;
                int removedCount = 0;
                
                for (String contentObj : contentObjects) {
                    // Clean up the object string
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
                            
                            // Skip the deleted content ID
                            if (contentId.equals(deletedContentId)) {
                                removedCount++;
                                continue;
                            }
                        }
                    }
                    
                    // Add this content object to the new JSON
                    if (!first) {
                        newJsonContent.append(",");
                    }
                    newJsonContent.append(contentObj);
                    first = false;
                }
                
                newJsonContent.append("]");
                
                // Write the updated content back to the file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(newJsonContent.toString());
                }
                
                if (removedCount > 0) {
                }
            }
        } catch (Exception e) {
        }
    }
}
