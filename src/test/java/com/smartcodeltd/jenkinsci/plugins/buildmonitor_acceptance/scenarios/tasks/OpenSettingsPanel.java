package com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.tasks;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.actions.Action;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

import static com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.actions.Click.click;

public class OpenSettingsPanel implements Action {

    public static OpenSettingsPanel openSettingsPanel() {
        return new OpenSettingsPanel();
    }

    @Override
    public void executeUsing(JenkinsRule jenkins, WebDriver browser) throws Exception {
        for (Action action : configurationPlan) {
            action.executeUsing(jenkins, browser);
            Thread.sleep(500);
        }
    }

    private List<Action> configurationPlan;

    private OpenSettingsPanel() {
        this.configurationPlan = new ArrayList<Action>() {{
            add(click(By.cssSelector("label[title=\"Configure Build Monitor Settings\"]")));
        }};
    }
}
