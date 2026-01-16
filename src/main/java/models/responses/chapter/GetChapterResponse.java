package models.responses.chapter;

import java.util.List;

public class GetChapterResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public Chapter chapterById;
    }

    public static class Chapter {
        public String id;
        public String title;
        public String description;
        public int order;
        public String programId;
        public String __typename;
    }

    public static class GraphQLError {
        public String message;
    }
}
