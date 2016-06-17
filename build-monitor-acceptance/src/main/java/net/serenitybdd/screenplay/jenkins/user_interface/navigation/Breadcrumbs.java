package net.serenitybdd.screenplay.jenkins.user_interface.navigation;

import net.serenitybdd.screenplay.jenkins.targets.Link;
import net.serenitybdd.screenplay.targets.Target;

import static java.lang.String.format;

public class Breadcrumbs {
    private Breadcrumbs(){}

    public static final Target Jenkins_Link = Link.to("Jenkins");

    public static Target linkTo(String name) {
        return Target.the(format("the '%s' breadcrumb link", name))
                .locatedBy("//ul[@id='breadcrumbs']//a[contains(., '{0}')]")
                .of(name);
    }
}
