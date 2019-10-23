package features;

import com.smartcodeltd.jenkinsci.plugins.build_monitor.questions.ProjectWidget;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.HaveABuildMonitorViewCreated;
import environment.JenkinsSandbox;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.serenitybdd.integration.jenkins.JenkinsInstance;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.jenkins.HaveAProjectCreated;
import net.serenitybdd.screenplayx.actions.Navigate;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Title;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isCurrentlyVisible;

@RunWith(SerenityRunner.class)
public class BuildMonitorShouldBeEasyToSetUp {

    Actor anna = Actor.named("Anna");

    @Managed public WebDriver browser;

    @Rule public JenkinsInstance jenkins = JenkinsSandbox.configure().create();

    @BeforeClass
    public static void setupChromeDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void actorCanBrowseTheWeb() {
        anna.can(BrowseTheWeb.with(browser));
    }

    @Test
    @Title("Adding a project to an empty Build Monitor")
    public void adding_project_to_an_empty_build_monitor() {

        givenThat(anna).wasAbleTo(
                Navigate.to(jenkins.url()),
                HaveAProjectCreated.called("My Awesome App")
        );

        when(anna).attemptsTo(HaveABuildMonitorViewCreated.showingAllTheProjects());

        then(anna).should(seeThat(ProjectWidget.of("My Awesome App").state(), isCurrentlyVisible()));
    }
}
