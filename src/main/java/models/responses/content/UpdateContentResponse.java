package models.responses.content;

import java.util.List;

public class UpdateContentResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public UpdateContent updateContent;
    }

    public static class UpdateContent {
        public String id;
        public String title;
        public String description;
        public String type;
        public Integer order;
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
