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

    private static final String MAIN_FOLDER_PROPERTY_NAME = "mainFolder";
    private static final String QAURL = "qaUrl";
    private static final String PRODURL = "prodUrl";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String EMAILUSERNAME = "emailUserName";
    private static final String EMAILPASSWORD = "emailPassword";
    private static final String MAIL_FOLDERS = "mailFolders";
    private static final String JIRAUSERNAME = "jiraUserName";
    private static final String JIRAPASSWORD = "jiraPassword";
    private static final String JIRAURL = "jiraURL";
    private static final String JIRAJQL = "jirajql";
    private static final String JIRAPROJECT = "jiraproject";
    private static final String JIRAENVIRONMENT = "jiraenvironment";
    private static final String JIRAISSUETYPEID = "jiraIssueTypeId";

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

    public static String getProdUrl() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(PRODURL);
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


    public static String getJiraJql() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(JIRAJQL);
    }

    public static String getJiraProject() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(JIRAPROJECT);
    }
    public static String getJiraEnvironment() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(JIRAENVIRONMENT);
    }

    public static String getJiraIssueTypeId() {
        LoadProperties properties = new LoadProperties();

        try {
            properties.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getPropertyValue(JIRAISSUETYPEID);
    }
}

