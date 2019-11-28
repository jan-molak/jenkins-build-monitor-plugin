package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.headline;

/**
 * @author Jan Molak
 */
public class HeadlineConfig {
    public final boolean displayCommittersOnBuildFailure;
    public final boolean displayCommittersOnFixedBuild;

    public HeadlineConfig(boolean displayCommittersOnBuildFailure, boolean displayCommittersOnFixedBuild) {

        this.displayCommittersOnBuildFailure = displayCommittersOnBuildFailure;
        this.displayCommittersOnFixedBuild = displayCommittersOnFixedBuild;
    }
}
