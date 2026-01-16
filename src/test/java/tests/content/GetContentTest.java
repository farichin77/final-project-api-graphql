package tests.content;

import core.BaseTest;
import io.restassured.response.Response;
import models.responses.content.GetContentResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.FilePaths;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import java.util.Map;

public class GetContentTest extends BaseTest {

    @BeforeClass
    public void setup() {
        AuthService.postLogin();
    }

    @Test
    public void testGetContentData() {
        String contentId = JsonHelper.getIdFromJson(FilePaths.CONTENT_DATA_JSON);
        Assert.assertNotNull(contentId, "No content ID found in JSON");

        Map<String, Object> variables = Map.of("id", contentId);
        String query = GraphQlFileReader.readQuery("GetContent.graphql");
        Response response = GraphQlClient.execute(query, variables);

        Assert.assertEquals(response.statusCode(), 200);

        GetContentResponse responseBody = response.as(GetContentResponse.class);
        Assert.assertNull(responseBody.errors, "Response has GraphQL errors");
        Assert.assertNotNull(responseBody.data.contentById, "Content data is null");
        Assert.assertEquals(responseBody.data.contentById.id, contentId);
    }
}
