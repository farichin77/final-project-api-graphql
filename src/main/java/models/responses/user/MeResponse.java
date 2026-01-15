package models.responses.user;

public class MeResponse {

  public Data data;

  public static class Data {
    public User me;
  }

  public static class User {
    public String id;
    public String name;
    public String email;
    public String phoneNumber;
    public String role;
  }
}
