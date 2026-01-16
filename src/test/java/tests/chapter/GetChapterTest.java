package tests.chapter;

import core.BaseTest;
import io.restassured.response.Response;
import models.responses.chapter.GetChapterResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.FilePaths;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import java.util.Map;

public class GetChapterTest extends BaseTest {

    @BeforeClass
    public void setup() {
        AuthService.postLogin();
    }

    @Test
    public void testGetChapterData() {
        String chapterId = JsonHelper.getLatestIdFromJson(FilePaths.CHAPTER_DATA_JSON);
        Assert.assertNotNull(chapterId, "No chapter ID found in JSON");

        Map<String, Object> variables = Map.of("id", chapterId);
        String query = GraphQlFileReader.readQuery("GetChapter.graphql");
        Response response = GraphQlClient.execute(query, variables);

        Assert.assertEquals(response.statusCode(), 200);

        GetChapterResponse responseBody = response.as(GetChapterResponse.class);
        Assert.assertNull(responseBody.errors, "Response has GraphQL errors");
        Assert.assertNotNull(responseBody.data.chapterById, "Chapter data is null");
        Assert.assertEquals(responseBody.data.chapterById.id, chapterId);
    }
}
