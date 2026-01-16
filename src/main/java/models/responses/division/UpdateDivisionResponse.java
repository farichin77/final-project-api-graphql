package models.responses.division;

import java.util.List;

public class UpdateDivisionResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public UpdateDivision updateDivision;
    }

    public static class UpdateDivision {
        public String id;
        public String name;
        public String __typename;
    }

    public static class GraphQLError {
        public String message;
    }
}
