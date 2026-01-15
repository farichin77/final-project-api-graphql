package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import models.responses.user.MeResponse;
import services.UserService;
import utils.ApiResponse;
import utils.Utils;

public class UserServiceTest extends BaseAuthenticatedTest {

  @Test
  public void authenticateUserCanGetMeQuery() {
    ApiResponse<MeResponse> response = UserService.getMe();
    MeResponse responseBody = response.getResponseBody();

    Assert.assertNotNull(responseBody.data.me.id);
    Assert.assertNotNull(responseBody.data.me.name);
    Utils.assertValidEmail(responseBody.data.me.email);
    Assert.assertNotNull(responseBody.data.me.phoneNumber);
    Assert.assertNotNull(responseBody.data.me.role);
  }
}
