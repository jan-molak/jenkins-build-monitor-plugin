package com.smartcodeltd.jenkinsci.plugins.buildmonitor;

import com.google.common.base.Objects;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.order.ByName;
import hudson.model.Job;

import java.util.Comparator;

import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.functions.NullSafety.getOrElse;

public class Config {

    private boolean colourBlindMode;
    private boolean displayCommitters;
    private boolean reduceMotion;
    private boolean showBadges;
    private int maxColumns;
    private double textScale;
    private BuildFailureAnalyzerDisplayedField buildFailureAnalyzerDisplayedField;
    
    public static Config defaultConfig() {
        Config defaultConfig = new Config();

        // Set default values
        defaultConfig.setColourBlindMode(false);
        defaultConfig.setDisplayCommitters(false);
        defaultConfig.setShowBadges(false);
        defaultConfig.setMaxColumns(2);
        defaultConfig.setTextScale(1.0);

        return defaultConfig;
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

    public boolean inColourBlindMode() {
        return colourBlindMode;
    }

    public void setColourBlindMode(boolean flag) {
        this.colourBlindMode = flag;
    }

    public boolean shouldDisplayCommitters() {
        return displayCommitters;
    }

    public void setDisplayCommitters(boolean flag) {
        this.displayCommitters = flag;
    }

    public boolean reduceMotion() {
        return reduceMotion;
    }

    public void setReduceMotion(boolean flag) {
        this.reduceMotion = flag;
    }

    public boolean showBadges() {
        return showBadges;
    }

    public void setShowBadges(boolean flag) {
        this.showBadges = flag;
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    public double getTextScale() {
        return textScale;
    }

    public void setTextScale(double scale) {
        this.textScale = scale;
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

    private Comparator<Job<?, ?>> order;
}
