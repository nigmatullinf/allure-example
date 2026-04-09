package io.qameta.allure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

/**
 * @author eroshenkoam (Artem Eroshenko).
 */
@Layer("API tests")
@Owner("testuser")
@Feature("Bugs")
public class IssuesTest {

    private static final String OWNER = "test-owner";
    private static final String REPO = "test-repo";

    private static final String ISSUE_TITLE = "Test issue title";

    private final WebSteps steps = new WebSteps();

    @BeforeEach
    public void startDriver() {
        steps.startDriver();
    }

    @Test
    @AllureId("171")
    @TM4J("AE-T6")
    @Microservice("API")
    @Story("Edit issue")
    @Tags({@Tag("WEB"), @Tag("smoke")})
    @JiraIssues({@JiraIssue("AE-3")})
    @DisplayName("Editing issue details")
    public void shouldAddLabelToIssue() {
        steps.openIssuesPage(OWNER, REPO);
        steps.createIssueWithTitle(ISSUE_TITLE);
        steps.shouldSeeIssueWithTitle(ISSUE_TITLE);
    }

    // @Test
    // @TM4J("AE-T5")
    // @Microservice("Repository")
    // @Story("Close existing issue")
    // @Tags({@Tag("web"), @Tag("regress4")})
    // @JiraIssues({@JiraIssue("AE-1")})
    // @DisplayName("Closing new issue for authorized user")
    // public void shouldCloseIssue() {
    //     steps.openIssuesPage(OWNER, REPO);
    //     steps.createIssueWithTitle(ISSUE_TITLE);
    //     steps.closeIssueWithTitle(ISSUE_TITLE);
    //     steps.shouldNotSeeIssueWithTitle(ISSUE_TITLE);
    // }

    @AfterEach
    public void stopDriver() {
        steps.stopDriver();
    }

}