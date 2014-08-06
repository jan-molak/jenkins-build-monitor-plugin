package com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.actions.Action;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.adapters.JenkinsAdapter;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.prerequisites.Prerequisite;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.WebDriver;

public class Scenario {
    public static Scenario using(JenkinsRule jenkins, WebDriver browser) {
        return new Scenario(jenkins, browser);
    }

    public Scenario WhenIHave(Prerequisite... prerequisites) throws Exception {
        return IHave(prerequisites);
    }

    public Scenario IHave(Prerequisite... prerequisites) throws Exception {
        Context currentContext = new Context(new JenkinsAdapter(jenkins.getInstance()));

        for(Prerequisite prerequisite : prerequisites) {
            currentContext = prerequisite.accept(currentContext);
        }

        return this;
    }

    public Scenario I(Action... plans) throws Exception {
        return WhenI(plans);
    }

    public Scenario WhenI(Action... plans) throws Exception {
        for(Action plan : plans) {
            plan.executeUsing(jenkins, browser);
        }

        return this;
    }

    private JenkinsRule jenkins;
    private WebDriver browser;

    private Scenario(JenkinsRule jenkins, WebDriver browser) {
        this.jenkins = jenkins;
        this.browser = browser;
    }
}
