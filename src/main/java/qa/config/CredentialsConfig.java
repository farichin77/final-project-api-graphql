package qa.config;

public class CredentialsConfig {

  public static final String EMAIL = EnvConfig.get("EMAIL");
  public static final String PASSWORD = EnvConfig.get("PASSWORD");
  public static final String COMPANY_ID = EnvConfig.get("COMPANY_ID");

  static {
    if (EMAIL.isBlank() || PASSWORD.isBlank() || COMPANY_ID.isBlank()) {
      throw new IllegalStateException(
          "Missing required auth environment variables: " +
              "EMAIL, PASSWORD, COMPANY_ID"
      );
    }
  }

  private CredentialsConfig() {
  }
}
