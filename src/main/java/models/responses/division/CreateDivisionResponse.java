package models.responses.division;

import java.util.List;

public class CreateDivisionResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public CreateDivision createDivision;
    }

    public static class CreateDivision {
        public String id;
        public String name;
        public String __typename;
    }

    public static class GraphQLError {
        public String message;
    }
}
