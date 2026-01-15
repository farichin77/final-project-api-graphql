package qa.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public static List<LoginTestData> readLoginTestData(String filePath) {
        if (filePath.endsWith(".csv")) {
            return readFromCSV(filePath);
        } else {
            return readFromExcel(filePath);
        }
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

    public static List<EmployeeTestData> readEmployeeTestData(String filePath) {
        List<EmployeeTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            long timestamp = System.currentTimeMillis();
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 14) {
                    String name = parts[0].trim();
                    String employeeId = parts[1].trim();
                    String email = parts[2].trim().replace("{timestamp}", String.valueOf(timestamp));
                    String phoneNumber = parts[3].trim();
                    String divisionId = parts[4].trim();
                    String employeeRole = parts[5].trim();
                    int angkatanId = parts[6].trim().isEmpty() ? 0 : Integer.parseInt(parts[6].trim());
                    String gender = parts[7].trim();
                    String dateOfBirth = parts[8].trim();
                    String address = parts[9].trim();
                    String nik = parts[10].trim();
                    String npwp = parts[11].trim();
                    String scenario = parts[12].trim();
                    String expectedResult = parts[13].trim();
                    
                    testData.add(new EmployeeTestData(name, employeeId, email, phoneNumber, 
                        divisionId, employeeRole, angkatanId, gender, dateOfBirth, address, 
                        nik, npwp, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read employee test data from CSV: " + filePath, e);
        }
        
        return testData;
    }
    
    private static List<LoginTestData> readFromCSV(String filePath) {
        List<LoginTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String email = parts[0].trim();
                    String password = parts[1].trim();
                    String companyId = parts[2].trim();
                    String scenario = parts[3].trim();
                    String expectedResult = parts[4].trim();
                    
                    testData.add(new LoginTestData(email, password, companyId, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + filePath, e);
        }
        
        return testData;
    }
    
    private static List<LoginTestData> readFromExcel(String filePath) {
        List<LoginTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new RuntimeException("Excel file not found: " + filePath);
            }
            
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row, start from row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell emailCell = row.getCell(0);
                Cell passwordCell = row.getCell(1);
                Cell companyIdCell = row.getCell(2);
                Cell scenarioCell = row.getCell(3);
                
                String email = getCellValue(emailCell);
                String password = getCellValue(passwordCell);
                String companyId = getCellValue(companyIdCell);
                String scenario = getCellValue(scenarioCell);
                String expectedResult = getCellValue(scenarioCell);
                
                testData.add(new LoginTestData(email, password, companyId, scenario, expectedResult));
            }
            
            workbook.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }
        
        return testData;
    }
    
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    public static class LoginTestData {
        public final String email;
        public final String password;
        public final String companyId;
        public final String scenario;
        public final String expectedResult;
        
        public LoginTestData(String email, String password, String companyId, String scenario, String expectedResult) {
            this.email = email;
            this.password = password;
            this.companyId = companyId;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static class EmployeeTestData {
        public final String name;
        public final String employeeId;
        public final String email;
        public final String phoneNumber;
        public final String divisionId;
        public final String employeeRole;
        public final int angkatanId;
        public final String gender;
        public final String dateOfBirth;
        public final String address;
        public final String nik;
        public final String npwp;
        public final String scenario;
        public final String expectedResult;

        public EmployeeTestData(String name, String employeeId, String email, String phoneNumber,
                               String divisionId, String employeeRole, int angkatanId, String gender,
                               String dateOfBirth, String address, String nik, String npwp,
                               String scenario, String expectedResult) {
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
    }

    public static List<DeleteEmployeeTestData> readDeleteEmployeeTestData(String filePath) {
        if (filePath.endsWith(".csv")) {
            return readDeleteEmployeeFromCSV(filePath);
        }
        return new ArrayList<>();
    }

    private static List<DeleteEmployeeTestData> readDeleteEmployeeFromCSV(String filePath) {
        List<DeleteEmployeeTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String employeeId = parts[0].trim();
                    String scenario = parts[1].trim();
                    String expectedResult = parts[2].trim();
                    
                    testData.add(new DeleteEmployeeTestData(employeeId, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read delete employee test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class DeleteEmployeeTestData {
        public final String employeeId;
        public final String scenario;
        public final String expectedResult;

        public DeleteEmployeeTestData(String employeeId, String scenario, String expectedResult) {
            this.employeeId = employeeId;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static List<DivisionTestData> readDivisionTestData(String filePath) {
        List<DivisionTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String name = parts[0].trim();
                    String description = parts[1].trim();
                    String scenario = parts[2].trim();
                    String expectedResult = parts[3].trim();
                    
                    testData.add(new DivisionTestData(name, description, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read division test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class DivisionTestData {
        public final String name;
        public final String description;
        public final String scenario;
        public final String expectedResult;

        public DivisionTestData(String name, String description, String scenario, String expectedResult) {
            this.name = name;
            this.description = description;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static List<UpdateDivisionTestData> readUpdateDivisionTestData(String filePath) {
        List<UpdateDivisionTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String description = parts[2].trim();
                    String scenario = parts[3].trim();
                    String expectedResult = parts[4].trim();
                    
                    testData.add(new UpdateDivisionTestData(id, name, description, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read update division test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class UpdateDivisionTestData {
        public final String id;
        public final String name;
        public final String description;
        public final String scenario;
        public final String expectedResult;

        public UpdateDivisionTestData(String id, String name, String description, String scenario, String expectedResult) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static List<UpdateEmployeeTestData> readUpdateEmployeeTestData(String filePath) {
        List<UpdateEmployeeTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 13) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String employeeId = parts[2].trim();
                    String email = parts[3].trim();
                    String phoneNumber = parts[4].trim();
                    String divisionId = parts[5].trim();
                    String employeeRole = parts[6].trim();
                    String angkatanId = parts[7].trim();
                    String gender = parts[8].trim();
                    String dateOfBirth = parts[9].trim();
                    String address = parts[10].trim();
                    String nik = parts[11].trim();
                    String npwp = parts[12].trim();
                    String scenario = parts[13].trim();
                    String expectedResult = parts[14].trim();
                    
                    testData.add(new UpdateEmployeeTestData(id, name, employeeId, email, phoneNumber, divisionId, employeeRole, angkatanId, gender, dateOfBirth, address, nik, npwp, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read update employee test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class UpdateEmployeeTestData {
        public final String id;
        public final String name;
        public final String employeeId;
        public final String email;
        public final String phoneNumber;
        public final String divisionId;
        public final String employeeRole;
        public final String angkatanId;
        public final String gender;
        public final String dateOfBirth;
        public final String address;
        public final String nik;
        public final String npwp;
        public final String scenario;
        public final String expectedResult;

        public UpdateEmployeeTestData(String id, String name, String employeeId, String email, String phoneNumber, String divisionId, String employeeRole, String angkatanId, String gender, String dateOfBirth, String address, String nik, String npwp, String scenario, String expectedResult) {
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
    }

    public static List<TrainingTestData> readTrainingTestData(String filePath) {
        List<TrainingTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String title = parts[0].trim();
                    String description = parts[1].trim();
                    String type = parts[2].trim();
                    String isSequential = parts[3].trim();
                    String scenario = parts[4].trim();
                    String expectedResult = parts[5].trim();
                    
                    testData.add(new TrainingTestData(title, description, type, Boolean.parseBoolean(isSequential), scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read training test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class TrainingTestData {
        public final String title;
        public final String description;
        public final String type;
        public final boolean isSequential;
        public final String scenario;
        public final String expectedResult;

        public TrainingTestData(String title, String description, String type, boolean isSequential, String scenario, String expectedResult) {
            this.title = title;
            this.description = description;
            this.type = type;
            this.isSequential = isSequential;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static List<UpdateTrainingTestData> readUpdateTrainingTestData(String filePath) {
        List<UpdateTrainingTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String id = parts[0].trim();
                    String title = parts[1].trim();
                    String description = parts[2].trim();
                    String type = parts[3].trim();
                    String isSequential = parts[4].trim();
                    String scenario = parts[5].trim();
                    String expectedResult = parts[6].trim();
                    
                    testData.add(new UpdateTrainingTestData(id, title, description, type, Boolean.parseBoolean(isSequential), scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read update training test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class UpdateTrainingTestData {
        public final String id;
        public final String title;
        public final String description;
        public final String type;
        public final boolean isSequential;
        public final String scenario;
        public final String expectedResult;

        public UpdateTrainingTestData(String id, String title, String description, String type, boolean isSequential, String scenario, String expectedResult) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.type = type;
            this.isSequential = isSequential;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static List<DeleteTrainingTestData> readDeleteTrainingTestData(String filePath) {
        List<DeleteTrainingTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String id = parts[0].trim();
                    String scenario = parts[1].trim();
                    String expectedResult = parts[2].trim();
                    
                    testData.add(new DeleteTrainingTestData(id, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read delete training test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class DeleteTrainingTestData {
        public final String id;
        public final String scenario;
        public final String expectedResult;

        public DeleteTrainingTestData(String id, String scenario, String expectedResult) {
            this.id = id;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static List<ChapterTestData> readChapterTestData(String filePath) {
        List<ChapterTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String title = parts[0].trim();
                    String description = parts[1].trim();
                    String order = parts[2].trim();
                    String programId = parts[3].trim();
                    String scenario = parts[4].trim();
                    String expectedResult = parts[5].trim();
                    
                    testData.add(new ChapterTestData(title, description, Integer.parseInt(order), programId, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read chapter test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class ChapterTestData {
        public final String title;
        public final String description;
        public final int order;
        public final String programId;
        public final String scenario;
        public final String expectedResult;

        public ChapterTestData(String title, String description, int order, String programId, String scenario, String expectedResult) {
            this.title = title;
            this.description = description;
            this.order = order;
            this.programId = programId;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static List<DeleteChapterTestData> readDeleteChapterTestData(String filePath) {
        List<DeleteChapterTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String id = parts[0].trim();
                    String scenario = parts[1].trim();
                    String expectedResult = parts[2].trim();
                    
                    testData.add(new DeleteChapterTestData(id, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read delete chapter test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class DeleteChapterTestData {
        public final String id;
        public final String scenario;
        public final String expectedResult;

        public DeleteChapterTestData(String id, String scenario, String expectedResult) {
            this.id = id;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static List<UpdateChapterTestData> readUpdateChapterTestData(String filePath) {
        List<UpdateChapterTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String id = parts[0].trim();
                    String title = parts[1].trim();
                    String description = parts[2].trim();
                    String order = parts[3].trim();
                    String programId = parts[4].trim();
                    String scenario = parts[5].trim();
                    String expectedResult = parts[6].trim();
                    
                    testData.add(new UpdateChapterTestData(id, title, description, Integer.parseInt(order), programId, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read update chapter test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class UpdateChapterTestData {
        public final String id;
        public final String title;
        public final String description;
        public final int order;
        public final String programId;
        public final String scenario;
        public final String expectedResult;

        public UpdateChapterTestData(String id, String title, String description, int order, String programId, String scenario, String expectedResult) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.order = order;
            this.programId = programId;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }

    public static List<ContentTestData> readContentTestData(String filePath) {
        List<ContentTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 10) {
                    String title = parts[0].trim();
                    String description = parts[1].trim();
                    String order = parts[2].trim();
                    String article = parts[3].trim();
                    String articleType = parts[4].trim();
                    String chapterId = parts[5].trim();
                    String duration = parts[6].trim();
                    String isRandomQuestion = parts[7].trim();
                    String mediaId = parts[8].trim();
                    String thumbnailUrl = parts[9].trim();
                    String type = parts[10].trim();
                    String scenario = parts[11].trim();
                    String expectedResult = parts[12].trim();
                    
                    testData.add(new ContentTestData(
                        title, description, 
                        order.isEmpty() ? null : Integer.parseInt(order),
                        article, articleType, chapterId,
                        duration.isEmpty() ? null : Integer.parseInt(duration),
                        Boolean.parseBoolean(isRandomQuestion),
                        mediaId, thumbnailUrl, type,
                        scenario, expectedResult
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read content test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class ContentTestData {
        public final String title;
        public final String description;
        public final Integer order;
        public final String article;
        public final String articleType;
        public final String chapterId;
        public final Integer duration;
        public final Boolean isRandomQuestion;
        public final String mediaId;
        public final String thumbnailUrl;
        public final String type;
        public final String scenario;
        public final String expectedResult;

        public ContentTestData(String title, String description, Integer order, String article, String articleType, 
                           String chapterId, Integer duration, Boolean isRandomQuestion, String mediaId, 
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
    }

    public static List<UpdateContentTestData> readUpdateContentTestData(String filePath) {
        List<UpdateContentTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 12) {
                    String id = parts[0].trim();
                    String title = parts[1].trim();
                    String description = parts[2].trim();
                    String order = parts[3].trim();
                    String article = parts[4].trim();
                    String articleType = parts[5].trim();
                    String chapterId = parts[6].trim();
                    String duration = parts[7].trim();
                    String isRandomQuestion = parts[8].trim();
                    String mediaId = parts[9].trim();
                    String thumbnailUrl = parts[10].trim();
                    String type = parts[11].trim();
                    String scenario = parts[12].trim();
                    String expectedResult = parts[13].trim();
                    
                    testData.add(new UpdateContentTestData(
                        id, title, description, 
                        order.isEmpty() ? null : Integer.parseInt(order),
                        article, articleType, chapterId,
                        duration.isEmpty() ? null : Integer.parseInt(duration),
                        Boolean.parseBoolean(isRandomQuestion),
                        mediaId, thumbnailUrl, type,
                        scenario, expectedResult
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read update content test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class UpdateContentTestData {
        public final String id;
        public final String title;
        public final String description;
        public final Integer order;
        public final String article;
        public final String articleType;
        public final String chapterId;
        public final Integer duration;
        public final Boolean isRandomQuestion;
        public final String mediaId;
        public final String thumbnailUrl;
        public final String type;
        public final String scenario;
        public final String expectedResult;

        public UpdateContentTestData(String id, String title, String description, Integer order, String article, String articleType, 
                               String chapterId, Integer duration, Boolean isRandomQuestion, String mediaId, 
                               String thumbnailUrl, String type, String scenario, String expectedResult) {
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
    }

    public static List<DeleteContentTestData> readDeleteContentTestData(String filePath) {
        List<DeleteContentTestData> testData = new ArrayList<>();
        
        try (InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + filePath);
            }
            
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String id = parts[0].trim();
                    String scenario = parts[1].trim();
                    String expectedResult = parts[2].trim();
                    
                    testData.add(new DeleteContentTestData(id, scenario, expectedResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read delete content test data from CSV: " + filePath, e);
        }
        
        return testData;
    }

    public static class DeleteContentTestData {
        public final String id;
        public final String scenario;
        public final String expectedResult;

        public DeleteContentTestData(String id, String scenario, String expectedResult) {
            this.id = id;
            this.scenario = scenario;
            this.expectedResult = expectedResult;
        }
    }
}
