package features;

import com.smartcodeltd.jenkinsci.plugins.build_monitor.questions.ProjectWidget;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.HaveABuildMonitorViewCreated;
import environment.JenkinsSandbox;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.serenitybdd.integration.jenkins.JenkinsInstance;
import net.serenitybdd.integration.jenkins.environment.rules.InstallPlugins;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.jenkins.HaveAnExternalProjectCreated;
import net.serenitybdd.screenplay.interacting_with_jenkins_api.abilities.InteractWithJenkinsAPI;
import net.serenitybdd.screenplay.interacting_with_jenkins_api.interactions.NotifyOfExternalProject;
import net.serenitybdd.screenplayx.actions.Navigate;
import net.thucydides.core.annotations.Managed;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static com.smartcodeltd.jenkinsci.plugins.build_monitor.matchers.ProjectInformationMatchers.displaysProjectStatusAs;
import static com.smartcodeltd.jenkinsci.plugins.build_monitor.model.ProjectStatus.Successful;
import static net.serenitybdd.screenplay.GivenWhenThen.*;

@RunWith(SerenityRunner.class)
public class ShouldSupportExternalProjects {

    Actor maggie = Actor.named("Maggie");

    @Managed public WebDriver browser;

    @Rule public JenkinsInstance jenkins = JenkinsSandbox.configure().afterStart(
      InstallPlugins.fromUpdateCenter("external-monitor-job")
    ).create();

    @BeforeClass
    public static void setupChromeDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void actorCanBrowseTheWeb() {
        maggie.can(BrowseTheWeb.with(browser))
              .can(InteractWithJenkinsAPI.using(jenkins.client()));
    }

    @Test
    public void visualising_an_external_project() throws Exception {

        givenThat(maggie).wasAbleTo(
                Navigate.to(jenkins.url()),
                HaveAnExternalProjectCreated.called("external-project")
        );

        when(maggie).attemptsTo(
                NotifyOfExternalProject.successOf("external-project"),
                HaveABuildMonitorViewCreated.showingAllTheProjects()
        );

        then(maggie).should(seeThat(ProjectWidget.of("external-project").information(),
                displaysProjectStatusAs(Successful)
        ));
    }
}
