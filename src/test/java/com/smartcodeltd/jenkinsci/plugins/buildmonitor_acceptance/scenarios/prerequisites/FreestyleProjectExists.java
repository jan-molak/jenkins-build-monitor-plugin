package com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.prerequisites;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.Context;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FreestyleProjectExists implements Prerequisite {

    public static FreestyleProjectExists aFreestyleProject(String name) {
        return new FreestyleProjectExists(name);
    }

    public FreestyleProjectExists configuredToRun(Builder... builders) {
        this.builders = Arrays.asList(builders);

        return this;
    }

    public FreestyleProjectExists executed() {
        shouldExecute = true;

        return this;
    }

    @Override
    public Context accept(Context context) throws Exception {
        context.createFreestyleProject(name, builders, shouldExecute);

        return context;
    }

    private final String name;

    private boolean shouldExecute  = false;
    private List<Builder> builders = new ArrayList<Builder>();

    private FreestyleProjectExists(String name) {
        this.name = name;
    }
}
