package loader;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.time.LocalDate.parse;

public class Util {

    private static final String TIME_STAMP_FORMAT = "yyyy-MM-dd_HH-mm-ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String MAIN_FOLDER_PROPERTY_NAME = "mainFolder";
    private static final String QAURL = "qaUrl";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String EMAILUSERNAME = "emailUserName";
    private static final String EMAILPASSWORD = "emailPassword";
    private static final String MAIL_FOLDERS = "mailFolders";
    private static final String JIRAUSERNAME = "jiraUserName";
    private static final String JIRAPASSWORD = "jiraPassword";
    private static final String JIRAURL = "jiraURL";


    public static String getTimeStamp() {
        return new SimpleDateFormat(TIME_STAMP_FORMAT).format(new Date());
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    public static LocalDate getDateFormatted(DateTime date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return parse(date.toString(), formatter);
    }

    public static boolean createDirectory(String path) {
        Path directoryPath = Paths.get(path);

        if (!Files.exists(directoryPath)) {
            try {
                Files.createDirectory(directoryPath);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public static String getMainFolder() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(MAIN_FOLDER_PROPERTY_NAME);
    }

    public static String getQaUrl() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(QAURL);
    }

    public static String getJiraUrl() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(JIRAURL);
    }


    public static String getLogin() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(LOGIN);
    }

    public static String getPassword() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(PASSWORD);
    }

    public static String getUserNameMail() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(EMAILUSERNAME);
    }

    public static String getPasswordMail() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(EMAILPASSWORD);
    }

    public static List<String> getMailFolders() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s = properties.getPropertyValue(MAIL_FOLDERS);
        return new ArrayList<>(Arrays.asList(s.split(",")));
    }


    public static List<String> getFilesInFolder(String folderPath) {

        List<String> files = new ArrayList<String>();

        if (folderPath != null) {
            File folder = new File(folderPath);

            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles();

                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        files.add(file.getAbsolutePath());
                    }
                }
            }
        }

        return files;
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File " + filePath + " was deleted.");
            }
        }
    }

    public static void deleteFiles(List<String> filePathes) {

        if (filePathes.size() > 0) {
            for (String filePath : filePathes) {
                deleteFile(filePath);
            }
        }
    }

    public static List<String> getListFilesInFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        List<String> pathes = new ArrayList<String>();

        for (File file : files) {
            pathes.add(file.getAbsolutePath());
        }

        return pathes;
    }

    public static void checkDirectory(String directory) throws FileNotFoundException {

        File file = new File(directory);

        if (!file.exists()) {
            if (file.mkdirs()) {
                System.out.println("Directory" + directory + " is created!");
            } else {
                throw new FileNotFoundException("directory '" + directory + "' not created.");
            }
        }
    }

    public static String getJiraLogin() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(JIRAUSERNAME);
    }

    public static String getJiraPassword() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(JIRAPASSWORD);
    }

}

