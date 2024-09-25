package io.qameta.allure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;

@org.junit.jupiter.api.DisplayName("TTempTests.class unit tests")
    public class MyExamples {


    /**
     * Uses a basic '@Description' annotation and a javaDoc method that allows you to pass text commented out over the test
     * Pros: Very handy if you're used to keeping documentation in code.
     * Cons: The description is not strongly tied to the test, there is a chance to make a mistake.
     */

    @Test
    @AllureId("9251")
    @DisplayName("Some test")
        @Description(useJavaDoc = true)
        public void test1667306661111() {
            step("some step name https://www");
            step("some step name www without https");
            step("* var serviceId1=\"temporaryRedirectWith www\"");
        step("step sleep 60", () -> {
            Thread.sleep(60000);
            step("sub step");
        });
        step(" temporaryRedirectWith www\n ");
        }

    @Test
    @DisplayName("new test")
    @Owner("daniil@qameta.io")
    @Feature("Issues")
    void testFromTestops() {
        step("step 1");
        step("step 2");
        step("step 3", () -> {
            step("sub step");
        });
        step("step sleep 60", () -> {
            Thread.sleep(60000);
            step("sub step");
        });

    }


}
