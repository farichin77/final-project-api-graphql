package tests.chapter;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.GraphQlFileReader;
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
    public void testUpdateChapterWithDataDriven(CsvReader.UpdateChapterTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get chapter ID from JSON file
        String chapterId = getChapterIdFromJson();
        if (chapterId == null) {
            Assert.fail("No chapter ID found in JSON file. Run ChapterDataDrivenTest first.");
        }
        
        // Get latest training ID from JSON
        String latestTrainingId = getLatestTrainingIdFromJson();
        if (latestTrainingId == null) {
            Assert.fail("No training ID found in JSON file. Run CreateTrainingTest first.");
        }
        
        // Replace placeholders with actual IDs
        String actualId = testData.id.equals("{lastCreatedId}") ? chapterId : testData.id;
        String actualProgramId = testData.programId.equals("{latestTrainingId}") ? latestTrainingId : testData.programId;
        
        System.out.println("=== Updating Chapter ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Chapter ID: " + actualId);
        System.out.println("Training ID: " + actualProgramId);
        System.out.println("New Title: " + testData.title);
        System.out.println("New Description: " + testData.description);
        System.out.println("New Order: " + testData.order);

        // Prepare variables for chapter update
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("title", testData.title);
        inputMap.put("description", testData.description);
        inputMap.put("order", testData.order);
        inputMap.put("programId", actualProgramId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", actualId);
        variables.put("input", inputMap);

        String query = GraphQlFileReader.readMutation("UpdateChapter.graphql");

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
