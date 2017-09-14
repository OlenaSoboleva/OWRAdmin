package groovy

import com.atlassian.jira.rest.client.JiraRestClient
import com.atlassian.jira.rest.client.JiraRestClientFactory
import com.atlassian.jira.rest.client.domain.BasicIssue
import com.atlassian.jira.rest.client.domain.Issue
import com.atlassian.jira.rest.client.domain.SearchResult
import com.atlassian.jira.rest.client.domain.Transition
import com.atlassian.jira.rest.client.domain.input.*
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.atlassian.util.concurrent.Promise
import loader.Util

class JiraTaskCreator {

    static JiraRestClient restClient;

    String jiraUserName
    String jiraPassword
    String jiraUrl
    String jiraJql
    String jiraProject
    String jiraEnvironment
    String jiraIssueTypeId

    JiraTaskCreator() {

        this.jiraUserName = Util.getJiraLogin()
        this.jiraPassword = Util.getJiraPassword()
        this.jiraUrl = Util.getJiraUrl()
        this.jiraJql = Util.getJiraJql()
        this.jiraProject = Util.getJiraProject()
        this.jiraIssueTypeId = Util.getJiraIssueTypeId()
    }
    public void jiraCreateSubTask(String subTaskfolder, File file) {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(jiraUrl);
        restClient = factory.createWithBasicHttpAuthentication(uri, jiraUserName, jiraPassword);

        String jql = jiraJql;
        int maxPerQuery = 1;
        int startIndex = 0;

        Promise<SearchResult> searchJqlPromiseTest = restClient.getSearchClient().searchJql(jql, maxPerQuery, startIndex);
        String project = jiraProject
        Long issueTypeId = Long.parseLong(jiraIssueTypeId)
        String summary = file.getName()+" Testing the Issue creation"
        String description = "Upload latest "+subTaskfolder+" to "+jiraEnvironment

        BasicIssue parentIssue = searchJqlPromiseTest.claim().getIssues()[0]
        println "Parent issue: "+parentIssue.key

        IssueInputBuilder issueBuilder = new IssueInputBuilder(project, issueTypeId, summary);
        issueBuilder.setDescription(description);
        issueBuilder.setFixVersions(restClient.getIssueClient().getIssue(parentIssue.key).claim().getFixVersions())

        String retailGroupId = restClient.getIssueClient().getIssue(parentIssue.key).claim().fields.findAll({ p -> p.name == "Retail Group" }).get(0).id
        String retailGroupValue = restClient.getIssueClient().getIssue(parentIssue.key).claim().fields.findAll({ p -> p.name == "Retail Group" }).get(0).getValue().getJSONObject(0).get("value");
        Map<String, Object> groupCustomField = new HashMap<String, Object>();
        groupCustomField.put("value",retailGroupValue) ;
        FieldInput groupField = new FieldInput(retailGroupId, Arrays.asList(new ComplexIssueInputFieldValue(groupCustomField)));
        issueBuilder.setFieldInput(groupField);

        IssueInput issueInput = issueBuilder.build();

        String environment = jiraEnvironment
        FieldInput fieldInputEnvironment = new FieldInput("environment", environment)
        issueInput.fields.put("environment", fieldInputEnvironment)
        generateIssueInput(issueInput, "parent", parentIssue.key)

        Promise<BasicIssue> promise = restClient.getIssueClient().createIssue(issueInput);
        BasicIssue basicIssue = promise.claim();
        Promise<Issue> promiseJavaIssue = restClient.getIssueClient().getIssue(basicIssue.getKey());
        Issue issue = promiseJavaIssue.claim();
        addAttachment(issue, file)
        final Transition transition = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).get().find({p ->p.name=="Done"})
        restClient.getIssueClient().transition(issue, new TransitionInput(transition.getId())).claim()
        System.out.println(String.format("New issue created is: %s\r\n", issue.getSummary() + "\n" + issue.key + "\n" + issue.self+ "\n" + issue.status))
    }

    private void addAttachment(Issue issue,File file){
        InputStream stream = new FileInputStream(file)
        URI attachmentURI = issue.getAttachmentsUri();
        restClient.issueClient.addAttachment(attachmentURI, stream, file.getName())
    }

    private void generateIssueInput(IssueInput issueInput, String fieldName, String id) {
        HashMap valuesMap = new HashMap()
        valuesMap.put("key", id)
        ComplexIssueInputFieldValue complexIssueInputFieldValue = new ComplexIssueInputFieldValue(valuesMap)
        FieldInput fieldInput = new FieldInput(fieldName, complexIssueInputFieldValue)
        issueInput.fields.put(fieldName, fieldInput)
    }
}
