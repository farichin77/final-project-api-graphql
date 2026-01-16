package models.responses.training;

import java.util.List;

public class GetProgramResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public Program programById;
    }

    public static class Program {
        public String id;
        public String title;
        public String description;
        public String type;
        public String __typename;
    }

    public static class GraphQLError {
        public String message;
    }
}
