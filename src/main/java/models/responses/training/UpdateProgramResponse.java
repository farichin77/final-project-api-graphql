package models.responses.training;

import java.util.List;

public class UpdateProgramResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public UpdateProgram updateProgram;
    }

    public static class UpdateProgram {
        public String id;
        public String title;
        public String description;
        public String type;
        public String __typename;
    }

    public static class GraphQLError {
        public String message;
        public List<Location> locations;
        public List<String> path;
        public Extensions extensions;
    }

    public static class Location {
        public int line;
        public int column;
    }

    public static class Extensions {
        public String code;
        public Exception exception;
    }

    public static class Exception {
        public List<String> stacktrace;
    }
}
