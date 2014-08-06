package com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.prerequisites;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.Context;

import java.util.Arrays;
import java.util.List;

public class FolderExists implements Prerequisite {

    public static FolderExists aFolder(String name) {
        return new FolderExists(name);
    }

    public Prerequisite containing(Prerequisite... prerequisites) {
        this.prerequisites = Arrays.asList(prerequisites);

        return this;
    }

    @Override
    public Context accept(Context context) throws Exception {
        Context currentContext = context.createFolder(name);

        for(Prerequisite p : prerequisites) {
            currentContext = p.accept(currentContext);
        }

        return currentContext;
    }

    private final String name;
    private List<Prerequisite> prerequisites;

    private FolderExists(String name) {
        this.name = name;
    }
}
