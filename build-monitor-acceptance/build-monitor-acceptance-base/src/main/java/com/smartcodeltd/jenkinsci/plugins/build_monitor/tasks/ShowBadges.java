package com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks;

import com.smartcodeltd.jenkinsci.plugins.build_monitor.user_interface.BuildMonitorDashboard;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.thucydides.core.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class ShowBadges implements Task {
    public static Task onTheDashboard() {
        return instrumented(ShowBadges.class);
    }

    @Step("{0} decides to display the badges on the dashboard")
    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            WaitUntil.the(BuildMonitorDashboard.Show_Badges, isVisible()),
            Click.on(BuildMonitorDashboard.Show_Badges)
        );
    }
}
