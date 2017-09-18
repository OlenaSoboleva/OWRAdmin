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
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class App {
    private static CloseableHttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
    private static String qaUrl = Util.getQaUrl();
    private static String prodURL = Util.getProdUrl();
    private static List<String> folders = Util.getMailFolders();

    public static void main(String[] args) throws IOException {
        for (int folderIndex = 0; folderIndex < folders.size(); folderIndex++) {

            JiraTaskCreator jiraTaskCreator = new JiraTaskCreator();
            AttachmentLoader attachmentLoader = new AttachmentLoader();
            EmailReplier emailReplier = new EmailReplier();
            Map<String, File> loadingMap = attachmentLoader.uploadAttachment(folders.get(folderIndex));
            Set<String> setFolders = loadingMap.keySet();
            File file;
            for (String folder : setFolders) {
                file = loadingMap.get(folder);
                Boolean resultUpload = httpPostFile(folder, qaUrl, file);
                if (resultUpload) {
                    //add production env
//                   httpPostFile(folder,prodURL,file);
                    jiraTaskCreator.jiraCreateSubTask(folder, file);
                }
                emailReplier.emailReply(folder, resultUpload);
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
}
