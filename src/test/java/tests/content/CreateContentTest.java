package tests.content;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.TestDataProvider;
import utils.GraphQlFileReader;
import io.restassured.response.Response;

import java.util.Map;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class CreateContentTest extends BaseTest {

    @Test(dataProvider = "contentTestData", dataProviderClass = TestDataProvider.class)
    public void testCreateContentWithDataDriven(CsvReader.ContentTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();

        // Get latest training ID from JSON
        String latestTrainingId = getLatestTrainingIdFromJson();
        if (latestTrainingId == null) {
            Assert.fail("No training ID found in JSON file. Run CreateTrainingTest first.");
        }

        // Get latest chapter ID from the latest training
        String latestChapterId = getLatestChapterIdFromTraining(latestTrainingId);
        if (latestChapterId == null) {
            Assert.fail("No chapter ID found for training " + latestTrainingId + ". Run CreateChapterTest first.");
        }

        // Replace placeholder with actual chapter ID
        String actualChapterId = testData.chapterId.equals("{latestChapterId}") ? latestChapterId : testData.chapterId;

        System.out.println("=== Creating Content ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Training ID: " + latestTrainingId);
        System.out.println("Chapter ID: " + actualChapterId);

        // Prepare variables for content creation
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("title", testData.title);
        inputMap.put("description", testData.description);
        inputMap.put("order", testData.order);
        inputMap.put("article", testData.article);
        inputMap.put("articleType", testData.articleType);
        inputMap.put("chapterId", actualChapterId);
        inputMap.put("duration", testData.duration);
        inputMap.put("isRandomQuestion", testData.isRandomQuestion);
        inputMap.put("mediaId", testData.mediaId);
        inputMap.put("thumbnailUrl", testData.thumbnailUrl);
        inputMap.put("type", testData.type);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", inputMap);

        String query = GraphQlFileReader.readMutation("CreateContent.graphql");

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
                System.out.println("⚠ Training JSON file not found: " + e.getMessage());
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
                
                System.out.println("✓ Found latest training ID: " + latestTrainingId);
                return latestTrainingId;
                
            } else if (content.startsWith("{") && content.endsWith("}")) {
                // Single object format: {"id":"uuid","title":"...","timestamp":123}
                int idStart = content.indexOf("\"id\":\"");
                if (idStart != -1) {
                    idStart += 6;
                    int idEnd = content.indexOf("\"", idStart);
                    if (idEnd != -1) {
                        String trainingId = content.substring(idStart, idEnd);
                        System.out.println("✓ Found training ID from single object: " + trainingId);
                        return trainingId;
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("⚠ Failed to read latest training ID from JSON: " + e.getMessage());
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
                System.out.println("⚠ Chapter JSON file not found: " + e.getMessage());
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
                            
                            // Extract timestamp to find the latest
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
                                        // Ignore timestamp parsing errors, use the chapter anyway
                                        if (latestChapterId == null) {
                                            latestChapterId = chapterId;
                                        }
                                    }
                                }
                            } else if (latestChapterId == null) {
                                latestChapterId = chapterId;
                            }
                        }
                    }
                    
                    searchIndex = objEnd + 1;
                } else {
                    break;
                }
            }
            
            System.out.println("✓ Found latest chapter ID: " + latestChapterId);
            return latestChapterId;
            
        } catch (Exception e) {
            System.out.println("⚠ Failed to read latest chapter ID from JSON: " + e.getMessage());
        }
        return null;
    }
}
