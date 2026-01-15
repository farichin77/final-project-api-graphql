package utils;

import org.testng.Assert;

public class Utils {

  public static void assertValidEmail(String email) {
    String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    Assert.assertTrue(
        email.matches(regex),
        "Invalid email format: " + email
    );
  }
}
