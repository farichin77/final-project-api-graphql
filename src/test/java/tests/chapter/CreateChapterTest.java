package tests.chapter;

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

public class CreateChapterTest extends BaseTest {

    @Test(dataProvider = "chapterTestData", dataProviderClass = TestDataProvider.class)
    public void testCreateChapterWithDataDriven(CsvReader.ChapterTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();

        // Get latest training ID from JSON
        String latestTrainingId = getLatestTrainingIdFromJson();
        if (latestTrainingId == null) {
            Assert.fail("No training ID found in JSON file. Run CreateTrainingTest first.");
        }

        // Replace placeholder with actual training ID
        String actualProgramId = testData.programId.equals("{latestTrainingId}") ? latestTrainingId : testData.programId;

        // Prepare variables for chapter creation
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("title", testData.title);
        inputMap.put("description", testData.description);
        inputMap.put("order", testData.order);
        inputMap.put("programId", actualProgramId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", inputMap);

        String query = GraphQlFileReader.readMutation("CreateChapter.graphql");

        System.out.println("=== Creating Chapter ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Training ID: " + actualProgramId);

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
                        saveChapterIdToJson(chapterId, testData.title, actualProgramId);
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

    private void saveChapterIdToJson(String chapterId, String chapterTitle, String trainingId) {
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
            
            // Parse and add new chapter with training ID
            String newChapterJson = "{\"id\":\"" + chapterId + "\",\"title\":\"" + chapterTitle + "\",\"programId\":\"" + trainingId + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
            
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
            System.out.println("✓ Training ID: " + trainingId);
            System.out.println("✓ Total chapters saved: " + chapterCount);
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to save chapter ID to JSON: " + e.getMessage());
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
                
                System.out.println("✓ Found latest training ID from array: " + latestTrainingId);
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
}
