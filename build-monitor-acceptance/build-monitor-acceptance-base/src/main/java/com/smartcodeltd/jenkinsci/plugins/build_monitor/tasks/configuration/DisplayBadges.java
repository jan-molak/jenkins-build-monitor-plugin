package com.smartcodeltd.jenkinsci.plugins.build_monitor.tasks.configuration;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.SelectFromOptions;
import net.serenitybdd.screenplay.jenkins.user_interface.ViewConfigurationPage;
import net.serenitybdd.screenplayx.actions.Scroll;
import net.thucydides.core.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class DisplayBadges implements Task {
    public static DisplayBadges asAUserSetting() {
        return instrumented(DisplayBadges.class, "User Setting");
    }

    public static DisplayBadges always() {
        return instrumented(DisplayBadges.class, "Always");
    }

    public static DisplayBadges never() {
        return instrumented(DisplayBadges.class, "Never");
    }

    @Step("{0} selects #text as the option to display badges")
    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Scroll.to(ViewConfigurationPage.Display_Badges),
                SelectFromOptions.byVisibleText(text).from(ViewConfigurationPage.Display_Badges)
        );
    }

    public DisplayBadges(String text) {
        this.text = text;
    }

    private final String text;
}
