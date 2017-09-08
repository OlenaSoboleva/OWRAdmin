package groovy

import com.atlassian.jira.rest.client.JiraRestClient
import com.atlassian.jira.rest.client.JiraRestClientFactory
import com.atlassian.jira.rest.client.domain.BasicIssue
import com.atlassian.jira.rest.client.domain.Issue
import com.atlassian.jira.rest.client.domain.SearchResult
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue
import com.atlassian.jira.rest.client.domain.input.FieldInput
import com.atlassian.jira.rest.client.domain.input.IssueInput
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.atlassian.util.concurrent.Promise
import loader.Util

class JiraTaskCreator {

    static JiraRestClient restClient;

    String jiraUserName
    String jiraPassword
    String jiraUrl

    JiraTaskCreator() {

        this.jiraUserName = Util.getJiraLogin()
        this.jiraPassword = Util.getJiraPassword()
        this.jiraUrl = Util.getJiraUrl()

    }

    public void jiraGetTask() {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(jiraUrl);
        restClient = factory.createWithBasicHttpAuthentication(uri, jiraUserName, jiraPassword);
//
//        String jql = "summary~'Test API sub-task' and status=open and project=UKWEBRIO and issuetype=11101";
        String jql = "issuekey=UKWEBRIO-12830";
        int maxPerQuery = 1;
        int startIndex = 0;

        Promise<SearchResult> searchJqlPromiseTest = restClient.getSearchClient().searchJql(jql, maxPerQuery, startIndex);
        String project = "UKWEBRIO"
        String summary = "Testing the Issue creation"
//        searchJqlPromiseTest.claim().getIssues().each { BasicIssue issue ->
//            System.out.println(issue.getKey());
//        }

        BasicIssue parentIssue = searchJqlPromiseTest.claim().getIssues()[0]
        println parentIssue.key


        IssueInputBuilder issueBuilder = new IssueInputBuilder("UKWEBRIO", 11110, summary);

        issueBuilder.setDescription(summary);

        issueBuilder.setFixVersions(restClient.getIssueClient().getIssue("UKWEBRIO-12830").claim().getFixVersions())
        IssueInput issueInput = issueBuilder.build();

        //   List value = restClient.getIssueClient().getIssue("UKWEBRIO-12830").claim().fields.findAll({p -> p.id == "parent"}).value

        HashMap valuesMap = new HashMap()
        valuesMap.put("key", "UKWEBRIO-12793")
        ComplexIssueInputFieldValue complexIssueInputFieldValue = new ComplexIssueInputFieldValue(valuesMap)
        FieldInput field = new FieldInput("parent", complexIssueInputFieldValue)

        issueInput.fields.put("parent", field)

        Promise<BasicIssue> promise = restClient.getIssueClient().createIssue(issueInput);
        BasicIssue basicIssue = promise.claim();
        Promise<Issue> promiseJavaIssue = restClient.getIssueClient().getIssue(basicIssue.getKey());

        Issue issue = promiseJavaIssue.claim();
        System.out.println(String.format("New issue created is: %s\r\n", issue.getSummary()));

    }

}
