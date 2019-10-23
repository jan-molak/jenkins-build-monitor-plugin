package features;

import com.smartcodeltd.jenkinsci.plugins.build_monitor.questions.ProjectWidget;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.HaveABuildMonitorViewCreated;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.ModifyControlPanelOptions;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.ShowBadges;
import environment.JenkinsSandbox;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.serenitybdd.integration.jenkins.JenkinsInstance;
import net.serenitybdd.integration.jenkins.environment.rules.InstallPlugins;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.jenkins.HaveAPipelineProjectCreated;
import net.serenitybdd.screenplay.jenkins.tasks.ScheduleABuild;
import net.serenitybdd.screenplay.jenkins.tasks.configuration.build_steps.GroovyScriptThat;
import net.serenitybdd.screenplay.jenkins.tasks.configuration.build_steps.SetPipelineDefinition;
import net.serenitybdd.screenplayx.actions.Navigate;
import net.thucydides.core.annotations.Managed;
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
public abstract class ShouldDisplayBadgesAbstractBase {

    Actor paul = Actor.named("Paul");

    @Managed public WebDriver browser;

    @Rule public JenkinsInstance jenkins = JenkinsSandbox.configure().afterStart(
            InstallPlugins.fromUpdateCenter(getPlugins())
    ).create();

    abstract String[] getPlugins();

    @BeforeClass
    public static void setupChromeDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void actorCanBrowseTheWeb() {
        paul.can(BrowseTheWeb.with(browser));
    }

    @Test
    public void displaying_build_badges() throws Exception {
        givenThat(paul).wasAbleTo(
                Navigate.to(jenkins.url()),
                HaveAPipelineProjectCreated.called("My App").andConfiguredTo(
                        SetPipelineDefinition.asFollows(GroovyScriptThat.Adds_A_Badge.code())
                ),

                ScheduleABuild.of("My App"),
                HaveABuildMonitorViewCreated.showingAllTheProjects()
        );

        when(paul).attemptsTo(ModifyControlPanelOptions.to(ShowBadges.onTheDashboard()));

        then(paul).should(seeThat(ProjectWidget.of("My App").badges(),
        		isCurrentlyVisible()
        ));
    }
}
