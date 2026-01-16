package models.responses.content;

import java.util.List;

public class GetContentResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public Content contentById;
    }

    public static class Content {
        public String id;
        public String title;
        public String description;
        public int order;
        public String chapterId;
        public String type;
        public String __typename;
    }

    public static class GraphQLError {
        public String message;
    }
}
