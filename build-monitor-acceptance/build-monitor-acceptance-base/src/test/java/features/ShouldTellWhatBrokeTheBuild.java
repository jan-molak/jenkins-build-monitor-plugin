package features;

import com.smartcodeltd.jenkinsci.plugins.build_monitor.questions.ProjectWidget;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.HaveABuildMonitorViewCreated;
import com.sonyericsson.jenkins.plugins.bfa.HaveAShellScriptFailureCauseDefined;
import environment.JenkinsSandbox;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.serenitybdd.integration.jenkins.JenkinsInstance;
import net.serenitybdd.integration.jenkins.environment.rules.InstallPlugins;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.jenkins.HaveAFailingProjectCreated;
import net.serenitybdd.screenplayx.actions.Navigate;
import net.thucydides.core.annotations.Managed;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static org.hamcrest.Matchers.containsString;

@RunWith(SerenityRunner.class)
public class ShouldTellWhatBrokeTheBuild {

    Actor dave = Actor.named("Dave");

    @Managed public WebDriver browser;

    @Rule public JenkinsInstance jenkins = JenkinsSandbox.configure().afterStart(
            InstallPlugins.fromUpdateCenter("cloudbees-folder", "build-failure-analyzer")
    ).create();

    @BeforeClass
    public static void setupChromeDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void actorCanBrowseTheWeb() {
        dave.can(BrowseTheWeb.with(browser));
    }

    @Test
    public void displaying_potential_failure_cause() throws Exception {
        givenThat(dave).wasAbleTo(
                Navigate.to(jenkins.url()),
                HaveAShellScriptFailureCauseDefined.called("Rogue AI").describedAs("Pod bay doors didn't open"),
                HaveAFailingProjectCreated.called("Discovery One")
        );

        when(dave).attemptsTo(HaveABuildMonitorViewCreated.showingAllTheProjects());

        then(dave).should(seeThat(ProjectWidget.of("Discovery One").details(),
                containsString("Identified problem: Rogue AI")
        ));
    }
}
