package loader;

import handler.AttachmentLoader;
import handler.EmailReplier;
import handler.JiraTaskCreator;
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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootApplication
public class AdminApplication implements ApplicationRunner {

    private static CloseableHttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
    private static String qaUrl = Util.getQaUrl();
    private static String prodURL = Util.getProdUrl();
    private static List<String> folders = Util.getMailFolders();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (int folderIndex = 0; folderIndex < folders.size(); folderIndex++) {

            JiraTaskCreator jiraTaskCreator = new JiraTaskCreator();
            AttachmentLoader attachmentLoader = new AttachmentLoader();
            EmailReplier emailReplier = new EmailReplier();
            Map<String, File> loadingMap = attachmentLoader.uploadAttachment(folders.get(folderIndex));
            Set<String> setFolders = loadingMap.keySet();
            File file;
            for (String folder : setFolders) {
                file = loadingMap.get(folder);
                Boolean uploadSuccessful = httpPostFile(folder, qaUrl, file);
                if (uploadSuccessful) {
                    uploadSuccessful= httpPostFile(folder, prodURL, file);
                    if (uploadSuccessful) {
                        jiraTaskCreator.jiraCreateSubTask(folder, file);
                    }
                    if (Util.isReplierEnabled()) {
                        emailReplier.emailReply(folder, uploadSuccessful);
                    }

                }}
        }
    }

    private static boolean httpPostFile(String folder, String url, File file) throws IOException {
        boolean uploadSuccessful = false;
        {
            HttpPost request = new HttpPost(url + "j_spring_security_check");
            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("j_password", Util.getPassword()));
            postParameters.add(new BasicNameValuePair("j_username", Util.getLogin()));
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
                uploadSuccessful = true;
            }
        }
        return uploadSuccessful;
    }

    private static String execute(HttpPost request) throws IOException {
        CloseableHttpResponse response = httpClient.execute(request);
        System.out.println(response);
        String responseString = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        System.out.println(responseString);
        return responseString;
    }

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
