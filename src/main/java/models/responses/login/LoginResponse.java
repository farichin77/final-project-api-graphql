package models.responses.login;

import java.util.List;

public class LoginResponse {

  public Data data;

  public static class Data {
    public Login login;
  }

  public static class Login {
    public User user;
    public List<Errors> errors;
  }

  public static class User {
    public String id;
    public String name;
    public String email;
    public String role;
    public String companyId;
  }

  public static class Errors {
    public String field;
    public String message;
  }
}
