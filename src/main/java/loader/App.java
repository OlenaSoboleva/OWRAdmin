package loader;

import groovy.AttachmentLoader;
import groovy.EmailReplier;
import groovy.JiraTaskCreator;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class App {
    private static CloseableHttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
    //    TODO: create and download in temp folder
    private static String baseDirLocation = Util.getMainFolder();
    private static String baseURL = Util.getQaUrl();

    public static void main(String[] args) throws IOException {
        Util.checkDirectory(baseDirLocation);
        JiraTaskCreator jiraTaskCreator = new JiraTaskCreator();
        AttachmentLoader attachmentLoader = new AttachmentLoader();
        EmailReplier emailReplier = new EmailReplier();
        List<String> folders = attachmentLoader.uploadAttachment();
        for (String folder : folders) {
            if (!Util.getFilesInFolder(getFolderPath(folder)).isEmpty()) {
                String fileName = getFileNameInFolder(folder);
                File file = new File(getFolderPath(folder) + "\\" + fileName);
                Boolean resultUpload = httpPostFile(folder, baseURL, file);
                if (resultUpload){
//                   httpPostFile(folder,prodURL,file);
                    jiraTaskCreator.jiraCreateSubTask(folder, file);
                    movefiles(folder);
                }
                emailReplier.emailReply(folder,resultUpload);
            }

        }
        System.exit(0);
    }

    private static boolean httpPostFile(String folder, String url, File file) throws IOException {
        boolean resultUpload = false;
        {
            HttpPost request = new HttpPost(url + "j_spring_security_check");
            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("j_password", Util.getLogin()));
            postParameters.add(new BasicNameValuePair("j_username", Util.getPassword()));
            request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            execute(request);
        }
        {
            HttpPost request = new HttpPost(url + "uploadFile?uploadCode=" + folder);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
                    .addTextBody("immediately", "Y", ContentType.TEXT_PLAIN);
            request.setEntity(builder.build());
            String result = execute(request);
            if (result.contains("Successfully uploaded the file")) {
                resultUpload = true;
            }
//            Assert.assertThat(result, CoreMatchers.containsString("Successfully uploaded the file"));
        }
        return resultUpload;
    }

    private static String execute(HttpPost request) throws IOException {
        System.out.println(request);
        if (request.getEntity().getContentLength() > 25600L) {
            System.out.println("Content is too long to display");
        } else {
            String content = IOUtils.toString(request.getEntity().getContent(), Charset.defaultCharset());
            System.out.println(content);
        }
        CloseableHttpResponse response = httpClient.execute(request);
        System.out.println(response);
        String responseString = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        System.out.println(responseString);
        return responseString;
    }

    private static String getFileNameInFolder(String folder) {
        File file = new File(baseDirLocation + "\\" + folder + "\\unprocessed");
        File[] listOfFiles = file.listFiles();
        return listOfFiles[0].getName();
    }

    private static String getFolderPath(String folder) {
        File file = new File(baseDirLocation + "\\" + folder + "\\unprocessed");
        return file.getAbsolutePath();
    }

    private static void movefiles(String folder) throws FileNotFoundException {
        File baseDir = new File(baseDirLocation + "\\" + folder + "\\unprocessed");
        File destDir = new File(baseDirLocation + "\\" + folder + "\\processed");
        File[] files = baseDir.listFiles();

        for (File file : files) {
            if (!file.getName().endsWith(".jar") || file.isDirectory()) {
                file.renameTo(new File(destDir, file.getName()));
                file.delete();
            }
        }
    }
}
