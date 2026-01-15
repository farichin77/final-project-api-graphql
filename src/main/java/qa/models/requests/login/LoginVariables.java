package qa.models.requests.login;

public class LoginVariables {

  public String usernameOrEmail;
  public String password;
  public String companyId;

  public LoginVariables(String usernameOrEmail, String password, String companyId) {
    this.usernameOrEmail = usernameOrEmail;
    this.password = password;
    this.companyId = companyId;
  }
}
