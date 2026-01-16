package core;

import client.AuthSession;
import org.testng.annotations.BeforeMethod;

public class BaseTest {

    @BeforeMethod
    public void resetSession() {
        AuthSession.setSessionCookie(null);
    }
}

