package models.responses.chapter;

import java.util.List;

public class UpdateChapterResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public UpdateChapter updateChapter;
    }

    public static class UpdateChapter {
        public String id;
        public String title;
        public String description;
        public int order;
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
