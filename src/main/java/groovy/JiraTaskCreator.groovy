package groovy

import com.atlassian.jira.rest.client.JiraRestClient
import com.atlassian.jira.rest.client.JiraRestClientFactory
import com.atlassian.jira.rest.client.domain.Issue
import com.atlassian.jira.rest.client.domain.SearchResult
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.atlassian.util.concurrent.Promise
import loader.Util

class JiraTaskCreator {
//    private static CloseableHttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
    static JiraRestClient restClient;

    String jiraUserName
    String jiraPassword

    JiraTaskCreator() {

        this.jiraUserName = Util.getJiraLogin()
        this.jiraPassword = Util.getJiraPassword()

    }

    public void jiraGetTask() {
//        HttpGet request = new HttpGet("https://tc-jira.atlassian.net/rest/api/2/");

        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI("https://tc-jira.atlassian.net/");
        restClient = factory.createWithBasicHttpAuthentication(uri, jiraUserName, jiraPassword);

        String jql = "summary~'Test API sub-task' and status=open and project=UKWEBRIO and issuetype=11101";
//        String jql= "project=UKWEBRIO";
        int maxPerQuery = 3;
        int startIndex = 0;

        Promise<SearchResult> searchJqlPromiseTest = restClient.getSearchClient().searchJql(jql,maxPerQuery,startIndex);
        searchJqlPromiseTest.claim().findAll().each { Issue issue ->
            System.out.println(issue.getKey());
        }
//
//        request.addHeader("Content-Type", "application/json");
//        request.addHeader("Accept", "application/json");
//        request.addHeader("Authorization", "Basic " + GetEncodedCredentials());
//
//
//        execute(request);
//
//        CloseableHttpResponse response = HttpClients.createMinimal().execute(request);
//        InputStream responseStream = response.getEntity().getContent();
//
//        println IOUtils.toString(responseStream, Charset.defaultCharset());

    }


//
//    private String GetEncodedCredentials() {
//        String mergedCredentials = String.format("{0}:{1}", jiraUserName, jiraPassword);
//        byte[] byteCredentials = mergedCredentials.getBytes("UTF-8");
//        return Base64.getEncoder().encodeToString(byteCredentials);
//    }
//
//    private static String execute(HttpGet request) throws IOException {
//        System.out.println(request);
//
//        String content = IOUtils.toString(request);
//        System.out.println(content);
//
//        CloseableHttpResponse response = httpClient.execute(request);
//        System.out.println(response);
//        String responseString = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
//        System.out.println(responseString);
//        return responseString;
//    }

}
