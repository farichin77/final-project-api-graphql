package models.responses.user;

public class MeResponse {
    public Data data;

    public static class Data {
        public Me me;
    }

    public static class Me {
        public String id;
        public String name;
        public String email;
        public String phoneNumber;
        public String role;
    }
}
