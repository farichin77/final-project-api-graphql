package tests.trainingModule.training;

import core.BaseTest;
import io.restassured.response.Response;
import models.responses.training.GetProgramResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.FilePaths;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import java.util.Map;

public class GetTrainingTest extends BaseTest {


    @Test
    public void testGetTrainingData() {
        String trainingId = JsonHelper.getLatestIdFromJson(FilePaths.TRAINING_DATA_JSON);
        Assert.assertNotNull(trainingId, "No training ID found in JSON");

        Map<String, Object> variables = Map.of("id", trainingId);
        String query = GraphQlFileReader.readQuery("GetTraining.graphql");
        Response response = GraphQlClient.execute(query, variables);

        Assert.assertEquals(response.statusCode(), 200);

        GetProgramResponse responseBody = response.as(GetProgramResponse.class);
        Assert.assertNull(responseBody.errors, "Response has GraphQL errors");
        Assert.assertNotNull(responseBody.data.programById, "Training data is null");
        Assert.assertEquals(responseBody.data.programById.id, trainingId);
    }
}
