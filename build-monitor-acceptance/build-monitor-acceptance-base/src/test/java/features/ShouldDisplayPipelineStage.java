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
import net.serenitybdd.screenplay.jenkins.HaveAPipelineProjectCreated;
import net.serenitybdd.screenplay.jenkins.tasks.ScheduleABuild;
import net.serenitybdd.screenplay.jenkins.tasks.configuration.build_steps.SetPipelineDefinition;
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
public class ShouldDisplayPipelineStage {

    Actor donald = Actor.named("Donald");

    @Managed public WebDriver browser;

    @Rule
    public JenkinsInstance jenkins = JenkinsSandbox.configure().afterStart(
            InstallPlugins.fromUpdateCenter("workflow-aggregator")
    ).create();

    @BeforeClass
    public static void setupChromeDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void actorCanBrowseTheWeb() {
        donald.can(BrowseTheWeb.with(browser));
    }

    @Test
    public void displaying_current_pipeline_stage() throws Exception {
        givenThat(donald).wasAbleTo(
                Navigate.to(jenkins.url()),
                HaveAPipelineProjectCreated.called("My Pipeline").andConfiguredTo(
                        SetPipelineDefinition.asFollows("stage('Compile') { sleep 50 }")
                ),

                ScheduleABuild.of("My Pipeline")
        );

        when(donald).attemptsTo(HaveABuildMonitorViewCreated.showingAllTheProjects());

        then(donald).should(seeThat(ProjectWidget.of("My Pipeline").pipelineStages(),
                containsString("[Compile]")
        ));
    }

}
