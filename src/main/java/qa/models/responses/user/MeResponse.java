package qa.models.responses.user;

public class MeResponse {

  public MeData data;

  public static class MeData {
    public User me;
  }

  public static class User {
    public String id;
    public String username;
    public String profilePictureUrl;
    public String name;
    public String email;
    public String role;
    public String companyId;
    public String phoneNumber;
    public String employeeRole;
    public String employeeId;
    public String gender;
    public String dateOfBirth;
    public String address;
    public String nik;
    public String npwp;
    public int countCertificate;
    public int countTrainingAssigned;
    public int countOnboardingAssigned;
    public int countAssignedVideoCourse;
    public int countAssignedProgram;
    public int countAssignedBootcamp;
    public String status;
    public boolean isMentor;
    public String division;
    public String __typename;
  }
}
