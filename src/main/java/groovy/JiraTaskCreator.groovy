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
        URI uri = new URI("https://tc-jira.atlassian.net/");
        restClient = factory.createWithBasicHttpAuthentication(uri, jiraUserName, jiraPassword);
//
        String jql = "summary~'Test API sub-task' and status!='Done' and project=UKWEBRIO and issuetype=11101";
//        String jql = "issuekey=UKWEBRIO-12830";
        int maxPerQuery = 1;
        int startIndex = 0;

        Promise<SearchResult> searchJqlPromiseTest = restClient.getSearchClient().searchJql(jql, maxPerQuery, startIndex);
        String project = "UKWEBRIO"
        Long issueTypeId = 11110
        String summary = "Testing the Issue creation"
        String description = "Upload latest Red Routes to UAT/LIVE"

        BasicIssue parentIssue = searchJqlPromiseTest.claim().getIssues()[0]
        println parentIssue.key

        IssueInputBuilder issueBuilder = new IssueInputBuilder(project, issueTypeId, summary);
        issueBuilder.setDescription(description);
        issueBuilder.setFixVersions(restClient.getIssueClient().getIssue(parentIssue.key).claim().getFixVersions())

        IssueInput issueInput = issueBuilder.build();
//        List value = restClient.getIssueClient().getIssue("UKWEBRIO-12793").claim().fields.findAll({p -> p.id == "parent"}).value
//        String sprintId = restClient.getIssueClient().getIssue(parentIssue.key).claim().fields.findAll({p -> p.name == "Sprint"}).get(0).id
        String retailGroupId = restClient.getIssueClient().getIssue(parentIssue.key).claim().fields.findAll({ p -> p.name == "Retail Group" }).get(0).id

//        String sprint = restClient.getIssueClient().getIssue(parentIssue.key).claim().fields.findAll({p -> p.name == "Sprint"}).get(0).getValue().getJSONObject(0).get("value")

        String environment = "UAT/LIVE"
        FieldInput fieldInput = new FieldInput("environment", environment)
        issueInput.fields.put("environment", fieldInput)

        String retailGroup = restClient.getIssueClient().getIssue(parentIssue.key).claim().fields.findAll({ p -> p.name == "Retail Group" }).get(0).getValue().getJSONObject(0).get("value")


        generateIssueInput(issueInput, "parent", parentIssue.key)
//        generateIssueInput(issueInput,environmentId,environment)
//        generateIssueInput(issueInput,sprintId,sprint)
        generateIssueInput(issueInput, retailGroupId, retailGroup)

        Promise<BasicIssue> promise = restClient.getIssueClient().createIssue(issueInput);
        BasicIssue basicIssue = promise.claim();

        Promise<Issue> promiseJavaIssue = restClient.getIssueClient().getIssue(basicIssue.getKey());

        Issue issue = promiseJavaIssue.claim();
        System.out.println(String.format("New issue created is: %s\r\n", issue.getSummary() + "\n" + issue.key + "\n" + issue.self));

    }

    private void generateIssueInput(IssueInput issueInput, String fieldName, String id) {
        HashMap valuesMap = new HashMap()
        valuesMap.put("key", id)//parentIssue.key
        ComplexIssueInputFieldValue complexIssueInputFieldValue = new ComplexIssueInputFieldValue(valuesMap)
        FieldInput parentField = new FieldInput(fieldName, complexIssueInputFieldValue)//"parent"

        issueInput.fields.put(fieldName, parentField)//"parent
    }

}
