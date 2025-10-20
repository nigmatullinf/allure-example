package io.qameta.allure;

import com.microsoft.playwright.*;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Playwright-based web steps with tracing support for CI
 * @author eroshenkoam (Artem Eroshenko).
 */
public class PlaywrightSteps {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private Tracing tracing;

    @Step("Starting Playwright browser")
    public void startDriver() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setSlowMo(100));
        
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setRecordVideoDir(Paths.get("build/playwright-videos"))
                .setRecordVideoSize(1920, 1080));
        
        // Start tracing
        tracing = context.tracing();
        tracing.start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        
        page = context.newPage();
        maybeThrowSeleniumTimeoutException();
    }

    @Step("Stopping Playwright browser")
    public void stopDriver() {
        try {
            if (tracing != null) {
                // Stop tracing and save trace file
                String traceFileName = "trace-" + System.currentTimeMillis() + ".zip";
                Path tracePath = Paths.get("build/playwright-traces", traceFileName);
                Files.createDirectories(tracePath.getParent());
                tracing.stop(new Tracing.StopOptions().setPath(tracePath));
                
                // Attach trace file to Allure report
                attachTraceFile(tracePath);
            }
            
            if (page != null) {
                page.close();
            }
            if (context != null) {
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            // Log error but don't fail the test
            System.err.println("Error stopping Playwright: " + e.getMessage());
        }
    }

    @Step("Open issues page `{owner}/{repo}`")
    public void openIssuesPage(final String owner, final String repo) {
        try {
            // Navigate to GitHub issues page
            String url = String.format("https://github.com/%s/%s/issues", owner, repo);
            page.navigate(url);
            page.waitForLoadState();
            
            // Take screenshot for Allure
            attachScreenshot("Issues page");
            attachPageSource();
            
            maybeThrowElementNotFoundException();
        } catch (Exception e) {
            attachScreenshot("Error opening issues page");
            throw new RuntimeException("Failed to open issues page: " + e.getMessage(), e);
        }
    }

    @Step("Open pull requests page `{owner}/{repo}`")
    public void openPullRequestsPage(final String owner, final String repo) {
        try {
            String url = String.format("https://github.com/%s/%s/pulls", owner, repo);
            page.navigate(url);
            page.waitForLoadState();
            
            attachScreenshot("Pull requests page");
            attachPageSource();
            
            maybeThrowElementNotFoundException();
        } catch (Exception e) {
            attachScreenshot("Error opening pull requests page");
            throw new RuntimeException("Failed to open pull requests page: " + e.getMessage(), e);
        }
    }

    @Step("Create pull request from branch `{branch}`")
    public void createPullRequestFromBranch(final String branch) {
        try {
            // Simulate creating a pull request
            page.click("text=New pull request");
            page.fill("[name='pull_request[title]']", "Pull request from " + branch);
            page.fill("[name='pull_request[body]']", "Description for pull request from " + branch);
            
            attachScreenshot("Creating pull request");
            maybeThrowElementNotFoundException();
        } catch (Exception e) {
            attachScreenshot("Error creating pull request");
            throw new RuntimeException("Failed to create pull request: " + e.getMessage(), e);
        }
    }

    @Step("Create issue with title `{title}`")
    public void createIssueWithTitle(String title) {
        try {
            // Simulate creating an issue
            page.click("text=New issue");
            page.fill("[name='issue[title]']", title);
            page.fill("[name='issue[body]']", "Description for issue: " + title);
            
            attachScreenshot("Creating issue");
            maybeThrowAssertionException(title);
        } catch (Exception e) {
            attachScreenshot("Error creating issue");
            throw new RuntimeException("Failed to create issue: " + e.getMessage(), e);
        }
    }

    @Step("Close pull request for branch `{branch}`")
    public void closePullRequestForBranch(final String branch) {
        try {
            // Simulate closing a pull request
            page.click("text=Close pull request");
            page.click("text=Confirm close");
            
            attachScreenshot("Closing pull request");
            maybeThrowAssertionException(branch);
        } catch (Exception e) {
            attachScreenshot("Error closing pull request");
            throw new RuntimeException("Failed to close pull request: " + e.getMessage(), e);
        }
    }

    @Step("Close issue with title `{title}`")
    public void closeIssueWithTitle(final String title) {
        try {
            // Simulate closing an issue
            page.click("text=Close issue");
            page.click("text=Confirm close");
            
            attachScreenshot("Closing issue");
            maybeThrowAssertionException(title);
        } catch (Exception e) {
            attachScreenshot("Error closing issue");
            throw new RuntimeException("Failed to close issue: " + e.getMessage(), e);
        }
    }

    @Step("Check pull request for branch `{branch}` exists")
    public void shouldSeePullRequestForBranch(final String branch) {
        try {
            // Check if pull request exists
            boolean exists = page.locator("text=" + branch).isVisible();
            attachScreenshot("Checking pull request exists");
            maybeThrowAssertionException(branch);
        } catch (Exception e) {
            attachScreenshot("Error checking pull request");
            throw new RuntimeException("Failed to check pull request: " + e.getMessage(), e);
        }
    }

    @Step("Check issue with title `{title}` exists")
    public void shouldSeeIssueWithTitle(final String title) {
        try {
            // Check if issue exists
            boolean exists = page.locator("text=" + title).isVisible();
            attachScreenshot("Checking issue exists");
            maybeThrowAssertionException(title);
        } catch (Exception e) {
            attachScreenshot("Error checking issue");
            throw new RuntimeException("Failed to check issue: " + e.getMessage(), e);
        }
    }

    @Step("Check pull request for branch `{branch}` not exists")
    public void shouldNotSeePullRequestForBranch(final String branch) {
        try {
            // Check if pull request doesn't exist
            boolean notExists = !page.locator("text=" + branch).isVisible();
            attachScreenshot("Checking pull request not exists");
            maybeThrowAssertionException(branch);
        } catch (Exception e) {
            attachScreenshot("Error checking pull request not exists");
            throw new RuntimeException("Failed to check pull request not exists: " + e.getMessage(), e);
        }
    }

    @Step("Check issue with title `{title}` not exists")
    public void shouldNotSeeIssueWithTitle(final String title) {
        try {
            // Check if issue doesn't exist
            boolean notExists = !page.locator("text=" + title).isVisible();
            attachScreenshot("Checking issue not exists");
            maybeThrowAssertionException(title);
        } catch (Exception e) {
            attachScreenshot("Error checking issue not exists");
            throw new RuntimeException("Failed to check issue not exists: " + e.getMessage(), e);
        }
    }

    @Attachment(value = "Screenshot", type = "image/png", fileExtension = "png")
    public byte[] attachScreenshot(String name) {
        try {
            if (page != null) {
                return page.screenshot();
            }
            return new byte[0];
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
            return new byte[0];
        }
    }

    @Attachment(value = "Page", type = "text/html", fileExtension = "html")
    public byte[] attachPageSource() {
        try {
            if (page != null) {
                return page.content().getBytes(Charset.forName("UTF-8"));
            }
            // Fallback to static HTML if page is not available
            final InputStream stream = ClassLoader.getSystemResourceAsStream("index.html");
            return IOUtils.toString(stream, Charset.forName("UTF-8")).getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Attachment(value = "Playwright Trace", type = "application/zip", fileExtension = "zip")
    public byte[] attachTraceFile(Path tracePath) {
        try {
            return Files.readAllBytes(tracePath);
        } catch (IOException e) {
            System.err.println("Failed to attach trace file: " + e.getMessage());
            return new byte[0];
        }
    }

    private void maybeThrowSeleniumTimeoutException() {
        if (isTimeToThrowException()) {
            fail(webDriverIsNotReachable("Allure"));
        }
    }

    private void maybeThrowElementNotFoundException() {
        try {
            Thread.sleep(1000);
            if (isTimeToThrowException()) {
                fail(elementNotFoundMessage("[//div[@class='something']]"));
            }
        } catch (InterruptedException e) {
            //do nothing, it's dummy test
        }
    }

    private void maybeThrowAssertionException(String text) {
        if (isTimeToThrowException()) {
            fail(textEqual(text, "another text"));
        }
    }

    private boolean isTimeToThrowException() {
        return new Random().nextBoolean()
                && new Random().nextBoolean()
                && new Random().nextBoolean()
                && new Random().nextBoolean();
    }

    private String webDriverIsNotReachable(final String text) {
        return String.format("WebDriverException: chrome not reachable\n" +
                "Element not found {By.xpath: //a[@href='/eroshenkoam/allure-example']}\n" +
                "Expected: text '%s'\n" +
                "Page source: file:/Users/eroshenkoam/Developer/eroshenkoam/webdriver-coverage-example/build/reports/tests/1603973861960.0.html\n" +
                "Timeout: 4 s.", text);
    }

    private String textEqual(final String expected, final String actual) {
        return String.format("Element should text '%s' {By.xpath: //a[@href='/eroshenkoam/allure-example']}\n" +
                "Element: '<a class=\"v-align-middle\">%s</a>'\n" +
                "Screenshot: file:/Users/eroshenkoam/Developer/eroshenkoam/webdriver-coverage-example/build/reports/tests/1603973703632.0.png\n" +
                "Page source: file:/Users/eroshenkoam/Developer/eroshenkoam/webdriver-coverage-example/build/reports/tests/1603973703632.0.html\n" +
                "Timeout: 4 s.\n", expected, actual);
    }

    private String elementNotFoundMessage(String selector) {
        return String.format("Element not found {By.xpath: %s}\n" +
                "Expected: visible or transparent: visible or have css value opacity=0\n" +
                "Screenshot: file:/Users/eroshenkoam/Developer/eroshenkoam/webdriver-coverage-example/build/reports/tests/1603973516437.0.png\n" +
                "Page source: file:/Users/eroshenkoam/Developer/eroshenkoam/webdriver-coverage-example/build/reports/tests/1603973516437.0.html\n" +
                "Timeout: 4 s.\n", selector);
    }
}
