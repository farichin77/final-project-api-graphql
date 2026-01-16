package models.responses;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonDeleteResponse {
    public Data data;
    public List<GraphQLError> errors;

    public static class Data {
        public Boolean deleteChapter;
        public Boolean deleteContent;
        public Boolean deleteProgram;
        public Boolean deleteEmployee;
    }

    public static class GraphQLError {
        public String message;
    }
}
