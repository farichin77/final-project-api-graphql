package models.responses.division;

import java.util.List;

public class GetDivisionResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public Division divisionById;
    }

    public static class Division {
        public String id;
        public String name;
        public String __typename;
    }

    public static class GraphQLError {
        public String message;
    }
}
