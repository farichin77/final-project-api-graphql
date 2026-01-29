package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public static class ChapterTestData {
        public String title;
        public String description;
        public int order;
        public String programId;
        public String scenario;
        public String expectedResult;

        public ChapterTestData(String title, String description, int order, String programId, String scenario, String expectedResult) {
            this.title = title;
            this.description = description;
            this.order = order;
            this.programId = programId;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "ChapterTestData{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", order=" + order +
                    ", programId='" + programId + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class UpdateChapterTestData {
        public String id;
        public String title;
        public String description;
        public int order;
        public String programId;
        public String scenario;
        public String expectedResult;

        public UpdateChapterTestData(String id, String title, String description, int order, String programId, String scenario, String expectedResult) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.order = order;
            this.programId = programId;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "UpdateChapterTestData{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", order=" + order +
                    ", programId='" + programId + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class DeleteChapterTestData {
        public String id;
        public String scenario;
        public String expectedResult;

        public DeleteChapterTestData(String id, String scenario, String expectedResult) {
            this.id = id;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "DeleteChapterTestData{" +
                    "id='" + id + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class ContentTestData {
        public String title;
        public String description;
        public int order;
        public String article;
        public String articleType;
        public String chapterId;
        public int duration;
        public boolean isRandomQuestion;
        public String mediaId;
        public String thumbnailUrl;
        public String type;
        public String scenario;
        public String expectedResult;

        public ContentTestData(String title, String description, int order, String article, String articleType, 
                           String chapterId, int duration, boolean isRandomQuestion, String mediaId, 
                           String thumbnailUrl, String type, String scenario, String expectedResult) {
            this.title = title;
            this.description = description;
            this.order = order;
            this.article = article;
            this.articleType = articleType;
            this.chapterId = chapterId;
            this.duration = duration;
            this.isRandomQuestion = isRandomQuestion;
            this.mediaId = mediaId;
            this.thumbnailUrl = thumbnailUrl;
            this.type = type;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "ContentTestData{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", order=" + order +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class UpdateContentTestData {
        public String id;
        public String title;
        public String description;
        public int order;
        public String article;
        public String articleType;
        public String chapterId;
        public int duration;
        public boolean isRandomQuestion;
        public String mediaId;
        public String thumbnailUrl;
        public String type;
        public String scenario;
        public String expectedResult;

        public UpdateContentTestData(String id, String title, String description, int order, String article, 
                                 String articleType, String chapterId, int duration, boolean isRandomQuestion, 
                                 String mediaId, String thumbnailUrl, String type, String scenario, String expectedResult) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.order = order;
            this.article = article;
            this.articleType = articleType;
            this.chapterId = chapterId;
            this.duration = duration;
            this.isRandomQuestion = isRandomQuestion;
            this.mediaId = mediaId;
            this.thumbnailUrl = thumbnailUrl;
            this.type = type;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "UpdateContentTestData{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", order=" + order +
                    ", type='" + type + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class DeleteContentTestData {
        public String id;
        public String scenario;
        public String expectedResult;

        public DeleteContentTestData(String id, String scenario, String expectedResult) {
            this.id = id;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "DeleteContentTestData{" +
                    "id='" + id + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class DivisionTestData {
        public String name;
        public String description;
        public String scenario;
        public String expectedResult;

        public DivisionTestData(String name, String description, String scenario, String expectedResult) {
            this.name = name;
            this.description = description;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "DivisionTestData{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class UpdateDivisionTestData {
        public String id;
        public String name;
        public String description;
        public String scenario;
        public String expectedResult;

        public UpdateDivisionTestData(String id, String name, String description, String scenario, String expectedResult) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "UpdateDivisionTestData{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class EmployeeTestData {
        public String name;
        public String employeeId;
        public String email;
        public String phoneNumber;
        public String divisionId;
        public String employeeRole;
        public int angkatanId;
        public String gender;
        public String dateOfBirth;
        public String address;
        public String nik;
        public String npwp;
        public String scenario;
        public String expectedResult;

        public EmployeeTestData(String name, String employeeId, String email, String phoneNumber, String divisionId,
                           String employeeRole, int angkatanId, String gender, String dateOfBirth,
                           String address, String nik, String npwp, String scenario, String expectedResult) {
            this.name = name;
            this.employeeId = employeeId;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.divisionId = divisionId;
            this.employeeRole = employeeRole;
            this.angkatanId = angkatanId;
            this.gender = gender;
            this.dateOfBirth = dateOfBirth;
            this.address = address;
            this.nik = nik;
            this.npwp = npwp;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "EmployeeTestData{" +
                    "name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class UpdateEmployeeTestData {
        public String id;
        public String name;
        public String employeeId;
        public String email;
        public String phoneNumber;
        public String divisionId;
        public String employeeRole;
        public String angkatanId;
        public String gender;
        public String dateOfBirth;
        public String address;
        public String nik;
        public String npwp;
        public String scenario;
        public String expectedResult;

        public UpdateEmployeeTestData(String id, String name, String employeeId, String email, String phoneNumber, 
                                  String divisionId, String employeeRole, String angkatanId, String gender, 
                                  String dateOfBirth, String address, String nik, String npwp, String scenario, String expectedResult) {
            this.id = id;
            this.name = name;
            this.employeeId = employeeId;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.divisionId = divisionId;
            this.employeeRole = employeeRole;
            this.angkatanId = angkatanId;
            this.gender = gender;
            this.dateOfBirth = dateOfBirth;
            this.address = address;
            this.nik = nik;
            this.npwp = npwp;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "UpdateEmployeeTestData{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class DeleteEmployeeTestData {
        public String id;
        public String scenario;
        public String expectedResult;

        public DeleteEmployeeTestData(String id, String scenario, String expectedResult) {
            this.id = id;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "DeleteEmployeeTestData{" +
                    "id='" + id + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class TrainingTestData {
        public String title;
        public String description;
        public String type;
        public boolean isSequential;
        public String scenario;
        public String expectedResult;

        public TrainingTestData(String title, String description, String type, boolean isSequential, String scenario, String expectedResult) {
            this.title = title;
            this.description = description;
            this.type = type;
            this.isSequential = isSequential;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "TrainingTestData{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", type='" + type + '\'' +
                    ", isSequential=" + isSequential +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class UpdateTrainingTestData {
        public String id;
        public String title;
        public String description;
        public String type;
        public boolean isSequential;
        public String scenario;
        public String expectedResult;

        public UpdateTrainingTestData(String id, String title, String description, String type, boolean isSequential, String scenario, String expectedResult) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.type = type;
            this.isSequential = isSequential;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "UpdateTrainingTestData{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class DeleteTrainingTestData {
        public String id;
        public String scenario;
        public String expectedResult;

        public DeleteTrainingTestData(String id, String scenario, String expectedResult) {
            this.id = id;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "DeleteTrainingTestData{" +
                    "id='" + id + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class LoginTestData {
        public String email;
        public String password;
        public String companyId;
        public String scenario;
        public String expectedResult;

        public LoginTestData(String email, String password, String companyId, String scenario, String expectedResult) {
            this.email = email;
            this.password = password;
            this.companyId = companyId;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "LoginTestData{" +
                    "email='" + email + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class AssignEmployeeTestData {
        public String employeeIds;
        public String programId;
        public String startDate;
        public String endDate;
        public String scenario;
        public String expectedResult;

        public AssignEmployeeTestData(String employeeIds, String programId, String startDate, String endDate, String scenario, String expectedResult) {
            this.employeeIds = employeeIds;
            this.programId = programId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "AssignEmployeeTestData{" +
                    "employeeIds='" + employeeIds + '\'' +
                    ", programId='" + programId + '\'' +
                    ", startDate='" + startDate + '\'' +
                    ", endDate='" + endDate + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static class TransferEmployeeTestData {
        public String employeeIds;
        public String divisionId;
        public String scenario;
        public String expectedResult;

        public TransferEmployeeTestData(String employeeIds, String divisionId, String scenario, String expectedResult) {
            this.employeeIds = employeeIds;
            this.divisionId = divisionId;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }

        @Override
        public String toString() {
            return "TransferEmployeeTestData{" +
                    "employeeIds='" + employeeIds + '\'' +
                    ", divisionId='" + divisionId + '\'' +
                    ", scenario='" + scenario + '\'' +
                    ", expectedResult='" + expectedResult + '\'' +
                    '}';
        }
    }

    public static List<ChapterTestData> readChapterTestData(String filePath) {
        List<ChapterTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 6) {
                    String title = values[0].trim();
                    String description = values[1].trim();
                    int order = values[2].trim().isEmpty() ? 0 : Integer.parseInt(values[2].trim());
                    String programId = values[3].trim();
                    String scenario = values[4].trim();
                    String expectedResult = values[5].trim();
                    
                    testDataList.add(new ChapterTestData(title, description, order, programId, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<UpdateChapterTestData> readUpdateChapterTestData(String filePath) {
        List<UpdateChapterTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 7) {
                    String id = values[0].trim();
                    String title = values[1].trim();
                    String description = values[2].trim();
                    int order = values[3].trim().isEmpty() ? 0 : Integer.parseInt(values[3].trim());
                    String programId = values[4].trim();
                    String scenario = values[5].trim();
                    String expectedResult = values[6].trim();
                    
                    testDataList.add(new UpdateChapterTestData(id, title, description, order, programId, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<DeleteChapterTestData> readDeleteChapterTestData(String filePath) {
        List<DeleteChapterTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String id = values[0].trim();
                    String scenario = values[1].trim();
                    String expectedResult = values[2].trim();
                    
                    testDataList.add(new DeleteChapterTestData(id, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<ContentTestData> readContentTestData(String filePath) {
        List<ContentTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 12) {
                    String title = values[0].trim();
                    String description = values[1].trim();
                    int order = values[2].trim().isEmpty() ? 0 : Integer.parseInt(values[2].trim());
                    String article = values[3].trim();
                    String articleType = values[4].trim();
                    String chapterId = values[5].trim();
                    int duration = values[6].trim().isEmpty() ? 0 : Integer.parseInt(values[6].trim());
                    boolean isRandomQuestion = Boolean.parseBoolean(values[7].trim());
                    String mediaId = values[8].trim();
                    String thumbnailUrl = values[9].trim();
                    String type = values[10].trim();
                    String scenario = values[11].trim();
                    String expectedResult = values[12].trim();
                    
                    testDataList.add(new ContentTestData(title, description, order, article, articleType, chapterId, 
                                                  duration, isRandomQuestion, mediaId, thumbnailUrl, type, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<UpdateContentTestData> readUpdateContentTestData(String filePath) {
        List<UpdateContentTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 14) {
                    String id = values[0].trim();
                    String title = values[1].trim();
                    String description = values[2].trim();
                    int order = values[3].trim().isEmpty() ? 0 : Integer.parseInt(values[3].trim());
                    String article = values[4].trim();
                    String articleType = values[5].trim();
                    String chapterId = values[6].trim();
                    int duration = values[7].trim().isEmpty() ? 0 : Integer.parseInt(values[7].trim());
                    boolean isRandomQuestion = Boolean.parseBoolean(values[8].trim());
                    String mediaId = values[9].trim();
                    String thumbnailUrl = values[10].trim();
                    String type = values[11].trim();
                    String scenario = values[12].trim();
                    String expectedResult = values[13].trim();
                    
                    testDataList.add(new UpdateContentTestData(id, title, description, order, article, articleType, 
                                                        chapterId, duration, isRandomQuestion, mediaId, thumbnailUrl, 
                                                        type, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<DeleteContentTestData> readDeleteContentTestData(String filePath) {
        List<DeleteContentTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String id = values[0].trim();
                    String scenario = values[1].trim();
                    String expectedResult = values[2].trim();
                    
                    testDataList.add(new DeleteContentTestData(id, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<DivisionTestData> readDivisionTestData(String filePath) {
        List<DivisionTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 4) {
                    String name = values[0].trim();
                    String description = values[1].trim();
                    String scenario = values[2].trim();
                    String expectedResult = values[3].trim();
                    
                    testDataList.add(new DivisionTestData(name, description, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<UpdateDivisionTestData> readUpdateDivisionTestData(String filePath) {
        List<UpdateDivisionTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 5) {
                    String id = values[0].trim();
                    String name = values[1].trim();
                    String description = values[2].trim();
                    String scenario = values[3].trim();
                    String expectedResult = values[4].trim();
                    
                    testDataList.add(new UpdateDivisionTestData(id, name, description, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<EmployeeTestData> readEmployeeTestData(String filePath) {
        List<EmployeeTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 14) {
                    String name = values[0].trim();
                    String employeeId = values[1].trim();
                    String email = values[2].trim();
                    String phoneNumber = values[3].trim();
                    String divisionId = values[4].trim();
                    String employeeRole = values[5].trim();
                    int angkatanId = values[6].trim().isEmpty() ? 0 : Integer.parseInt(values[6].trim());
                    String gender = values[7].trim();
                    String dateOfBirth = values[8].trim();
                    String address = values[9].trim();
                    String nik = values[10].trim();
                    String npwp = values[11].trim();
                    String scenario = values[12].trim();
                    String expectedResult = values[13].trim();
                     
                    testDataList.add(new EmployeeTestData(name, employeeId, email, phoneNumber, divisionId, employeeRole, 
                                                   angkatanId, gender, dateOfBirth, address, nik, npwp, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<UpdateEmployeeTestData> readUpdateEmployeeTestData(String filePath) {
        List<UpdateEmployeeTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 15) {
                    String id = values[0].trim();
                    String name = values[1].trim();
                    String employeeId = values[2].trim();
                    String email = values[3].trim();
                    String phoneNumber = values[4].trim();
                    String divisionId = values[5].trim();
                    String employeeRole = values[6].trim();
                    String angkatanId = values[7].trim();
                    String gender = values[8].trim();
                    String dateOfBirth = values[9].trim();
                    String address = values[10].trim();
                    String nik = values[11].trim();
                    String npwp = values[12].trim();
                    String scenario = values[13].trim();
                    String expectedResult = values[14].trim();
                    
                    testDataList.add(new UpdateEmployeeTestData(id, name, employeeId, email, phoneNumber, divisionId, 
                                                         employeeRole, angkatanId, gender, dateOfBirth, 
                                                         address, nik, npwp, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<DeleteEmployeeTestData> readDeleteEmployeeTestData(String filePath) {
        List<DeleteEmployeeTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String id = values[0].trim();
                    String scenario = values[1].trim();
                    String expectedResult = values[2].trim();
                    
                    testDataList.add(new DeleteEmployeeTestData(id, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<TrainingTestData> readTrainingTestData(String filePath) {
        List<TrainingTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 6) {
                    String title = values[0].trim();
                    String description = values[1].trim();
                    String type = values[2].trim();
                    boolean isSequential = Boolean.parseBoolean(values[3].trim());
                    String scenario = values[4].trim();
                    String expectedResult = values[5].trim();
                    
                    testDataList.add(new TrainingTestData(title, description, type, isSequential, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<UpdateTrainingTestData> readUpdateTrainingTestData(String filePath) {
        List<UpdateTrainingTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 7) {
                    String id = values[0].trim();
                    String title = values[1].trim();
                    String description = values[2].trim();
                    String type = values[3].trim();
                    boolean isSequential = Boolean.parseBoolean(values[4].trim());
                    String scenario = values[5].trim();
                    String expectedResult = values[6].trim();
                    
                    testDataList.add(new UpdateTrainingTestData(id, title, description, type, isSequential, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<DeleteTrainingTestData> readDeleteTrainingTestData(String filePath) {
        List<DeleteTrainingTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String id = values[0].trim();
                    String scenario = values[1].trim();
                    String expectedResult = values[2].trim();
                    
                    testDataList.add(new DeleteTrainingTestData(id, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<LoginTestData> readLoginTestData(String filePath) {
        List<LoginTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 5) {
                    String email = values[0].trim();
                    String password = values[1].trim();
                    String companyId = values[2].trim();
                    String scenario = values[3].trim();
                    String expectedResult = values[4].trim();
                    
                    testDataList.add(new LoginTestData(email, password, companyId, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<LoginTestData> readPositiveLoginTestData(String filePath) {
        List<LoginTestData> allTestData = readLoginTestData(filePath);
        List<LoginTestData> positiveTestData = new ArrayList<>();
        
        for (LoginTestData data : allTestData) {
            if ("SUCCESS".equalsIgnoreCase(data.expectedResult)) {
                positiveTestData.add(data);
            }
        }
        
        return positiveTestData;
    }

    public static List<AssignEmployeeTestData> readAssignEmployeeTestData(String filePath) {
        List<AssignEmployeeTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 6) {
                    String employeeIds = values[0].trim();
                    String programId = values[1].trim();
                    String startDate = values[2].trim();
                    String endDate = values[3].trim();
                    String scenario = values[4].trim();
                    String expectedResult = values[5].trim();
                    
                    testDataList.add(new AssignEmployeeTestData(employeeIds, programId, startDate, endDate, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }

    public static List<TransferEmployeeTestData> readTransferEmployeeTestData(String filePath) {
        List<TransferEmployeeTestData> testDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 4) {
                    String employeeIds = values[0].trim();
                    String divisionId = values[1].trim();
                    String scenario = values[2].trim();
                    String expectedResult = values[3].trim();

                    testDataList.add(new TransferEmployeeTestData(employeeIds, divisionId, scenario, expectedResult));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testDataList;
    }
}
