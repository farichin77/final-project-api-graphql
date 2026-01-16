package tests.chapter;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.CsvReader.DeleteChapterTestData;
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
    public void testDeleteChapterWithDataDriven(DeleteChapterTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get all chapter IDs from JSON file
        List<String> chapterIds = getAllChapterIdsFromJson();
        if (chapterIds.isEmpty()) {
            Assert.fail("No chapter IDs found in JSON file. Run ChapterDataDrivenTest first.");
        }
        
        System.out.println("=== Deleting All Chapters ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Total chapters to delete: " + chapterIds.size());
        
        int deletedCount = 0;
        for (String chapterId : chapterIds) {
            System.out.println("Deleting Chapter ID: " + chapterId);
            
            // Execute delete mutation
            Map<String, Object> variables = Map.of("id", chapterId);
            String deletePayload = "mutation deleteChapter($id: String!) {\n  deleteChapter(id: $id)\n}";
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

    private List<String> getAllChapterIdsFromJson() {
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
                System.out.println("⚠ File not found: " + e.getMessage());
                return chapterIds;
            }
            
            String content = jsonContent.toString().trim();
            if (content.isEmpty()) {
                return chapterIds;
            }
            
            // Parse JSON manually
            if (content.startsWith("[") && content.endsWith("]")) {
                // Array format: [{"id":"uuid1",...},{"id":"uuid2",...}]
                String arrayContent = content.substring(1, content.length() - 1);
                String[] chapterObjects = arrayContent.split("\\},\\{");
                
                for (String chapterObj : chapterObjects) {
                    // Clean up the object string
                    if (!chapterObj.startsWith("{")) {
                        chapterObj = "{" + chapterObj;
                    }
                    if (!chapterObj.endsWith("}")) {
                        chapterObj = chapterObj + "}";
                    }
                    
                    // Extract ID using string manipulation
                    int idStart = chapterObj.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6; // length of "id":"
                        int idEnd = chapterObj.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String chapterId = chapterObj.substring(idStart, idEnd);
                            chapterIds.add(chapterId);
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
                        String chapterId = content.substring(idStart, idEnd);
                        chapterIds.add(chapterId);
                    }
                }
            }
            
            System.out.println("✓ Read " + chapterIds.size() + " chapter IDs from JSON");
            for (String id : chapterIds) {
                System.out.println("  - " + id);
            }
            
        } catch (Exception e) {
            System.out.println("⚠ Failed to read chapter IDs from JSON: " + e.getMessage());
        }
        return chapterIds;
    }
}
