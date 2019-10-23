package com.smartcodeltd.jenkinsci.plugins.buildmonitor;

import com.google.common.base.Objects;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.build.GetBuildViewModel;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.build.GetLastBuild;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.order.ByName;
import hudson.model.Job;

import java.util.Comparator;

import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.functions.NullSafety.getOrElse;

public class Config {

    private boolean displayCommitters;
    private DisplayOptions displayBadges;
    private GetBuildViewModel displayBadgesFrom;
    private BuildFailureAnalyzerDisplayedField buildFailureAnalyzerDisplayedField;
    
    public static Config defaultConfig() {
        return new Config();
    }

    public Comparator<Job<?, ?>> getOrder() {
        /*
         * Jenkins unmarshals objects from config.xml by setting their private fields directly and without invoking their constructors.
         * In order to retrieve a potentially already persisted field try to first get the field, if that didn't work - use defaults.
         *
         * This is defensive coding to avoid issues such as this one:
         *  https://github.com/jan-molak/jenkins-build-monitor-plugin/issues/43
         */

        return getOrElse(order, new ByName());
    }

    public void setOrder(Comparator<Job<?, ?>> order) {
        this.order = order;
    }
    
    public BuildFailureAnalyzerDisplayedField getBuildFailureAnalyzerDisplayedField() {
        return getOrElse(buildFailureAnalyzerDisplayedField, BuildFailureAnalyzerDisplayedField.Name);
    }
    
    public void setBuildFailureAnalyzerDisplayedField(String buildFailureAnalyzerDisplayedField) {
        this.buildFailureAnalyzerDisplayedField = BuildFailureAnalyzerDisplayedField.valueOf(buildFailureAnalyzerDisplayedField);
    }
    
    public boolean shouldDisplayCommitters() {
        return getOrElse(displayCommitters, true);
    }

    public void setDisplayCommitters(boolean flag) {
        this.displayCommitters = flag;
    }

    public DisplayOptions getDisplayBadges() {
        return getOrElse(displayBadges, DisplayOptions.UserSetting);
    }

    public void setDisplayBadges(String option) {
        this.displayBadges = DisplayOptions.valueOf(option);
    }
    
    public GetBuildViewModel getDisplayBadgesFrom() {
        return getOrElse(displayBadgesFrom, new GetLastBuild());
    }

    public void setDisplayBadgesFrom(GetBuildViewModel displayBadgesFrom) {
        this.displayBadgesFrom = displayBadgesFrom;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("order", order.getClass().getSimpleName())
                .toString();
    }

    // --

    public enum BuildFailureAnalyzerDisplayedField {
        Name("name"),
        Description("description"),
        None("none");
    
        private final String value;
        BuildFailureAnalyzerDisplayedField(String value) {
            this.value = value;
        }
    
        public String getValue() { return value; }
    
        @Override
        public String toString() { return value; }
    }
    
    public enum DisplayOptions {
        Always, Never, UserSetting;
    }

    private Comparator<Job<?, ?>> order;
}