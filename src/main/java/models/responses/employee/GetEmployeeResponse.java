package models.responses.employee;

import java.util.List;

public class GetEmployeeResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public Employee employeeById;
    }

    public static class Employee {
        public String id;
        public String profilePictureUrl;
        public String employeeId;
        public String name;
        public String gender;
        public String dateOfBirth;
        public String email;
        public String phoneNumber;
        public String address;
        public Division division;
        public String employeeRole;
        public Double angkatanId;
        public Angkatan angkatan;
        public String nik;
        public String npwp;
        public String status;
        public String __typename;
    }

    public static class Division {
        public String id;
        public String name;
        public String __typename;
    }

    public static class Angkatan {
        public Double id;
        public String name;
        public String __typename;
    }

    public static class GraphQLError {
        public String message;
    }
}
