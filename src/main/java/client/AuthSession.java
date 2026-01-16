package client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AuthSession {

  private static String sessionCookie;
  private static final String COOKIE_FILE = "session_cookie.tmp";

  public static void setSessionCookie(String sid) {
    sessionCookie = sid;
    try {
      if (sid != null) {
        Files.writeString(Paths.get(COOKIE_FILE), sid);
      } else {
        Files.deleteIfExists(Paths.get(COOKIE_FILE));
      }
    } catch (IOException e) {
      // Ignore
    }
  }

  public static String getSessionCookie() {
    if (sessionCookie == null || sessionCookie.isEmpty()) {
      try {
        if (Files.exists(Paths.get(COOKIE_FILE))) {
          sessionCookie = Files.readString(Paths.get(COOKIE_FILE)).trim();
        }
      } catch (IOException e) {
        // Ignore
      }
    }
    return sessionCookie;
  }

  public static boolean isSessionActive() {
    String sessionCookie = getSessionCookie();
    if (sessionCookie == null || sessionCookie.isEmpty()) {
      return false;
    }
    return true; // Simplified for now to avoid recursion
  }
}
