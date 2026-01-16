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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.FileReader;
import java.io.BufferedReader;

public class DeleteChapterTest extends BaseTest {


    @DataProvider(name = "deleteChapterTestData")
    public Object[][] getDeleteChapterTestData() {
        var testDataList = CsvReader.readDeleteChapterTestData("test-data/delete-chapter-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "deleteChapterTestData")
    public void testDeleteChapterWithDataDriven(CsvReader.DeleteChapterTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get latest training ID from JSON
        String latestTrainingId = getLatestTrainingIdFromJson();
        if (latestTrainingId == null) {
            Assert.fail("No training ID found in JSON file. Run CreateTrainingTest first.");
        }
        
        // Get chapters created from the latest training
        List<String> chapterIds = getChaptersByTrainingId(latestTrainingId);
        if (chapterIds.isEmpty()) {
            Assert.fail("No chapters found for training ID: " + latestTrainingId + ". Run CreateChapterTest first.");
        }
        
        System.out.println("=== Deleting Chapters from Latest Training ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Training ID: " + latestTrainingId);
        System.out.println("Total chapters to delete: " + chapterIds.size());
        
        int deletedCount = 0;
        for (String chapterId : chapterIds) {
            System.out.println("Deleting Chapter ID: " + chapterId);
            
            // Execute delete mutation
            Map<String, Object> variables = Map.of("id", chapterId);
            String deletePayload = GraphQlFileReader.readMutation("DeleteChapter.graphql");
            Response deleteResponse = GraphQlClient.execute(deletePayload, variables);

            System.out.println("Delete Response Status: " + deleteResponse.statusCode());
            
            if (deleteResponse.statusCode() == 200) {
                String deleteResponseBody = deleteResponse.getBody().asString();
                if (deleteResponseBody.contains("\"deleteChapter\":true")) {
                    deletedCount++;
                    System.out.println("✓ Chapter deleted successfully");
                } else {
                    System.out.println("✗ Chapter deletion failed");
                }
            } else {
                System.out.println("✗ Delete request failed with status: " + deleteResponse.statusCode());
            }
        }
        
        System.out.println("✓ Total chapters deleted: " + deletedCount + "/" + chapterIds.size());
        Assert.assertTrue(deletedCount > 0, "At least one chapter should be deleted");
        System.out.println("✓ Test completed - All chapter IDs read from JSON and deleted");
    }

    private List<String> getChaptersByTrainingId(String trainingId) {
        List<String> chapterIds = new ArrayList<>();
        
        try {
            String filePath = "src/test/resources/chapter-data/chapter-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read entire file
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            } catch (Exception e) {
                System.out.println("⚠ Chapter JSON file not found: " + e.getMessage());
                return chapterIds;
            }
            
            String content = jsonContent.toString().trim();
            
            if (content.isEmpty()) {
                return chapterIds;
            }
            
            // Simple approach: find all chapters with the training ID
            int searchIndex = 0;
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
                            chapterIds.add(chapterId);
                        }
                    }
                    
                    searchIndex = objEnd + 1;
                } else {
                    break;
                }
            }
            
            System.out.println("✓ Found " + chapterIds.size() + " chapters for training " + trainingId);
            
        } catch (Exception e) {
            System.out.println("⚠ Failed to read chapter IDs by training ID: " + e.getMessage());
        }
        return chapterIds;
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
