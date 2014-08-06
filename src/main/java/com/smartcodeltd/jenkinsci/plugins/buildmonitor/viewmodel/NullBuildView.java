package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel;

import hudson.model.Result;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NullBuildView implements BuildViewModel {

    @Override
    public String name() {
        return "";
    }

    @Override
    public String url() {
        return "";
    }

    @Override
    public Result result() {
        return Result.NOT_BUILT;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public Duration elapsedTime() {
        return null;
    }

    @Override
    public Duration duration() {
        return null;
    }

    @Override
    public Duration estimatedDuration() {
        return null;
    }

    @Override
    public int progress() {
        return 0;
    }

    @Override
    public boolean hasPreviousBuild() {
        return false;
    }

    @Override
    public BuildViewModel previousBuild() {
        return null;
    }

    @Override
    public Set<User> culprits() {
        return new HashSet<User>();
    }

    @Override
    public boolean isClaimed() {
        return false;
    }

    @Override
    public String claimant() {
        return null;
    }

    @Override
    public String reasonForClaim() {
        return null;
    }

    @Override
    public boolean hasKnownFailures() {
        return false;
    }

    @Override
    public List<String> knownFailures() {
        return null;
    }
}
