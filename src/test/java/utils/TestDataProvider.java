package utils;

import org.testng.annotations.DataProvider;

public class TestDataProvider {

    @DataProvider(name = "chapterTestData")
    public static Object[][] getChapterTestData() {
        var testDataList = CsvReader.readChapterTestData("test-data/chapter-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "updateChapterTestData")
    public static Object[][] getUpdateChapterTestData() {
        var testDataList = CsvReader.readUpdateChapterTestData("test-data/update-chapter-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "deleteChapterTestData")
    public static Object[][] getDeleteChapterTestData() {
        var testDataList = CsvReader.readDeleteChapterTestData("test-data/delete-chapter-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "contentTestData")
    public static Object[][] getContentTestData() {
        var testDataList = CsvReader.readContentTestData("test-data/content-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "updateContentTestData")
    public static Object[][] getUpdateContentTestData() {
        var testDataList = CsvReader.readUpdateContentTestData("test-data/update-content-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "deleteContentTestData")
    public static Object[][] getDeleteContentTestData() {
        var testDataList = CsvReader.readDeleteContentTestData("test-data/delete-content-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "divisionTestData")
    public static Object[][] getDivisionTestData() {
        var testDataList = CsvReader.readDivisionTestData("test-data/division-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "updateDivisionTestData")
    public static Object[][] getUpdateDivisionTestData() {
        var testDataList = CsvReader.readUpdateDivisionTestData("test-data/update-division-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "deleteDivisionTestData")
    public static Object[][] getDeleteDivisionTestData() {
        // Since readDeleteDivisionTestData doesn't exist in CsvReader, 
        // this data provider is not implemented
        return new Object[0][0];
    }

    @DataProvider(name = "employeeTestData")
    public static Object[][] getEmployeeTestData() {
        var testDataList = CsvReader.readEmployeeTestData("test-data/employee-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "updateEmployeeTestData")
    public static Object[][] getUpdateEmployeeTestData() {
        var testDataList = CsvReader.readUpdateEmployeeTestData("test-data/update-employee-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "deleteEmployeeTestData")
    public static Object[][] getDeleteEmployeeTestData() {
        var testDataList = CsvReader.readDeleteEmployeeTestData("test-data/delete-employee-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "trainingTestData")
    public static Object[][] getTrainingTestData() {
        var testDataList = CsvReader.readTrainingTestData("test-data/training-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "updateTrainingTestData")
    public static Object[][] getUpdateTrainingTestData() {
        var testDataList = CsvReader.readUpdateTrainingTestData("test-data/update-training-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "deleteTrainingTestData")
    public static Object[][] getDeleteTrainingTestData() {
        var testDataList = CsvReader.readDeleteTrainingTestData("test-data/delete-training-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "loginTestData")
    public static Object[][] getLoginTestData() {
        var testDataList = CsvReader.readLoginTestData("test-data/login-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @DataProvider(name = "positiveLoginTestData")
    public static Object[][] getPositiveLoginTestData() {
        var testDataList = CsvReader.readPositiveLoginTestData("test-data/login-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }
}
