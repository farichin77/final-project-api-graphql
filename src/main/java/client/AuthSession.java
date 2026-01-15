package client;

import models.responses.user.MeResponse;
import services.UserService;
import utils.ApiResponse;

public class AuthSession {

  private static String sessionCookie;

  public static void setSessionCookie(String sid) {
    sessionCookie = sid;
  }

  public static String getSessionCookie() {
    return sessionCookie;
  }

  public static boolean isSessionActive() {
    String sessionCookie = getSessionCookie();

    // Fast fail when cookie is not set
    if (sessionCookie == null || sessionCookie.isEmpty()) {
      return false;
    }

    ApiResponse<MeResponse> meResponse = UserService.getMe();
    return meResponse.getStatusCode() == 200;
  }
}
