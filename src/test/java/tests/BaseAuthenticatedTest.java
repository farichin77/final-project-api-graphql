package tests;

import org.testng.annotations.BeforeSuite;
import services.AuthService;

public abstract class BaseAuthenticatedTest {

  @BeforeSuite
  public void authenticate() {
    AuthService.postLogin();
  }
}
