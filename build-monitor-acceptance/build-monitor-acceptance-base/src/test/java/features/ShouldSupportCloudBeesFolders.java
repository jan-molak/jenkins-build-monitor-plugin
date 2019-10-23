package features;

import com.cloudbees.hudson.plugins.folder.HaveAFolderCreated;
import com.cloudbees.hudson.plugins.folder.HaveANestedProjectCreated;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.questions.ProjectWidget;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.CreateABuildMonitorView;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.configuration.DisplayAllProjects;
import com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.configuration.DisplayNestedProjects;
import environment.JenkinsSandbox;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.serenitybdd.integration.jenkins.JenkinsInstance;
import net.serenitybdd.integration.jenkins.environment.rules.InstallPlugins;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplayx.actions.Navigate;
import net.thucydides.core.annotations.Managed;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

@RunWith(SerenityRunner.class)
public class ShouldSupportCloudBeesFolders {

    Actor anna = Actor.named("Anna");

    @Managed public WebDriver browser;

    @Rule public JenkinsInstance jenkins = JenkinsSandbox.configure().afterStart(
            InstallPlugins.fromUpdateCenter("cloudbees-folder")
    ).create();

    @BeforeClass
    public static void setupChromeDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void actorCanBrowseTheWeb() {
        anna.can(BrowseTheWeb.with(browser));
    }

    @Test
    public void visualising_projects_nested_in_folders() throws Exception {

        givenThat(anna).wasAbleTo(
                Navigate.to(jenkins.url()),
                HaveAFolderCreated.called("Search Services").andInsideIt(
                        HaveANestedProjectCreated.called("Librarian"),
                        HaveAFolderCreated.called("Contracts").andInsideIt(
                                HaveANestedProjectCreated.called("Third Party System")
                        )
                )
        );

        when(anna).attemptsTo(
                CreateABuildMonitorView.called("Build Monitor").andConfigureItTo(
                        DisplayAllProjects.usingARegularExpression(),
                        DisplayNestedProjects.fromSubfolders()
                )
        );

        then(anna).should(seeThat(ProjectWidget.of("Search Services » Librarian").state(), isVisible()));
        then(anna).should(seeThat(ProjectWidget.of("Search Services » Contracts » Third Party System").state(), isVisible()));
    }
}
