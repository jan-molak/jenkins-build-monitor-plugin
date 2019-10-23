package com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.configuration;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.SelectFromOptions;
import net.serenitybdd.screenplay.jenkins.user_interface.ViewConfigurationPage;
import net.serenitybdd.screenplayx.actions.Scroll;
import net.thucydides.core.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class DisplayBadgesFrom implements Task {
    public static DisplayBadgesFrom theLastBuild() {
        return instrumented(DisplayBadgesFrom.class, "Last Build");
    }

    public static DisplayBadgesFrom theLastCompletedBuild() {
        return instrumented(DisplayBadgesFrom.class, "Last Completed Build");
    }

    @Step("{0} selects to display badges from #text")
    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Scroll.to(ViewConfigurationPage.Display_Badges_From),
                SelectFromOptions.byVisibleText(text).from(ViewConfigurationPage.Display_Badges_From)
        );
    }

    public DisplayBadgesFrom(String text) {
        this.text = text;
    }

    private final String text;
}
