package handler

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
import org.apache.commons.io.FilenameUtils

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
        this.jiraEnvironment = Util.getJiraEnvironment()

    }
    public void jiraCreateSubTask(String subTaskfolder, File file) {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(jiraUrl);
        restClient = factory.createWithBasicHttpAuthentication(uri, jiraUserName, jiraPassword);
        BasicIssue parentIssue = getParentIssue(restClient)
        IssueInput issueInput = setIssueFields(file, subTaskfolder, restClient, parentIssue)
        Issue issue = createIssue(restClient, issueInput)
        addAttachment(issue, file)
        changeIssueStatus(restClient, issue)
        System.out.println(String.format("New issue created is: %s\r\n", issue.getSummary() + "\n" + issue.key + "\n" + issue.self+ "\n" + issue.status))
    }

    private getParentIssue(JiraRestClient restClient) {
        String jql = jiraJql;
        int maxPerQuery = 1;
        int startIndex = 0;

        Promise<SearchResult> searchJqlPromiseTest = restClient.getSearchClient().searchJql(jql, maxPerQuery, startIndex);
        BasicIssue parentIssue = searchJqlPromiseTest.get().getIssues()[0]
        println "Parent issue: " + parentIssue.key
        parentIssue
    }

    private setIssueFields(File file, String subTaskfolder, JiraRestClient restClient, BasicIssue parentIssue) {
        String project = jiraProject
        Long issueTypeId = Long.parseLong(jiraIssueTypeId)
        String summary = FilenameUtils.removeExtension(file.getName())
        String description = "Upload latest " + subTaskfolder + " to " + jiraEnvironment
        IssueInputBuilder issueBuilder = new IssueInputBuilder(project, issueTypeId, summary);
        issueBuilder.setDescription(description);
        issueBuilder.setFixVersions(restClient.getIssueClient().getIssue(parentIssue.key).get().getFixVersions())
        FieldInput groupField = generateRetailGroupFieldInput(restClient, parentIssue)
        issueBuilder.setFieldInput(groupField);
        IssueInput issueInput = issueBuilder.build();
        generateEnvironmentIssueInput(issueInput)
        generateParentIssueInput(issueInput, "parent", parentIssue.key)
        issueInput
    }

    private createIssue(JiraRestClient restClient, IssueInput issueInput) {
        Promise<BasicIssue> promise = restClient.getIssueClient().createIssue(issueInput);
        BasicIssue basicIssue = promise.get();
        Promise<Issue> promiseJavaIssue = restClient.getIssueClient().getIssue(basicIssue.getKey());
        Issue issue = promiseJavaIssue.get();
        issue
    }

    private void addAttachment(Issue issue,File file){
        InputStream stream = new FileInputStream(file)
        URI attachmentURI = issue.getAttachmentsUri();
        restClient.issueClient.addAttachment(attachmentURI, stream, file.getName())
    }

    private changeIssueStatus(JiraRestClient restClient, Issue issue) {
        final Transition transition = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).get().find({ p -> p.name == "Done" })
        restClient.getIssueClient().transition(issue, new TransitionInput(transition.getId())).get()
    }

    private generateRetailGroupFieldInput(JiraRestClient restClient, BasicIssue parentIssue) {
        String retailGroupId = restClient.getIssueClient().getIssue(parentIssue.key).get().fields.findAll({ p -> p.name == "Retail Group" }).get(0).id
        String retailGroupValue = restClient.getIssueClient().getIssue(parentIssue.key).get().fields.findAll({ p -> p.name == "Retail Group" }).get(0).getValue().getJSONObject(0).get("value");
        Map<String, Object> groupCustomField = new HashMap<String, Object>();
        groupCustomField.put("value", retailGroupValue);
        FieldInput groupField = new FieldInput(retailGroupId, Arrays.asList(new ComplexIssueInputFieldValue(groupCustomField)));
        groupField
    }

    private generateEnvironmentIssueInput(IssueInput issueInput) {
        String environment = jiraEnvironment
        FieldInput fieldInputEnvironment = new FieldInput("environment", environment)
        issueInput.fields.put("environment", fieldInputEnvironment)
    }

    private void generateParentIssueInput(IssueInput issueInput, String fieldName, String id) {
        HashMap valuesMap = new HashMap()
        valuesMap.put("key", id)
        ComplexIssueInputFieldValue complexIssueInputFieldValue = new ComplexIssueInputFieldValue(valuesMap)
        FieldInput fieldInput = new FieldInput(fieldName, complexIssueInputFieldValue)
        issueInput.fields.put(fieldName, fieldInput)
    }
}
