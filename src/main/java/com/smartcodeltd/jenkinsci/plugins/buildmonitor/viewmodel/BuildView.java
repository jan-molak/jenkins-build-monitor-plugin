package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.BuildAugmentor;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.bfa.Analysis;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.claim.Claim;
import hudson.Functions;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jenkins.model.Jenkins;

public class BuildView implements BuildViewModel {

    private final Run<?,?> build;
    private final Date systemTime;
    private final BuildAugmentor augmentor;

    public static BuildView of(Run<?, ?> build) {
        return new BuildView(build, new BuildAugmentor(), new Date());
    }

    public static BuildView of(Run<?, ?> build, BuildAugmentor augmentor, Date systemTime) {
        return new BuildView(build, augmentor, systemTime);
    }


    @Override
    public String name() {
        return build.getDisplayName();
    }

    @Override
    public String url() {
        return build.getUrl();
    }

    @Override
    public Result result() {
        return build.getResult();
    }

    @Override
    public boolean isRunning() {
        return isRunning(this.build);
    }

    private boolean isRunning(Run<?, ?> build) {
        return (build.hasntStartedYet() || build.isBuilding() || build.isLogUpdated());
    }

    @Override
    public Duration elapsedTime() {
        return new Duration(now() - whenTheBuildStarted());
    }

    @Override
    public Duration duration() {
        return new Duration(build.getDuration());
    }

    @Override
    public Duration estimatedDuration() {
        return new Duration(build.getEstimatedDuration());
    }

    @Override
    public int progress() {
        if (! isRunning()) {
            return 0;
        }

        if (isTakingLongerThanUsual()) {
            return 100;
        }

        long elapsedTime       = now() - whenTheBuildStarted(),
             estimatedDuration = build.getEstimatedDuration();

        if (estimatedDuration > 0) {
            return (int) ((float) elapsedTime / (float) estimatedDuration * 100);
        }

        return 100;
    }

    private boolean isTakingLongerThanUsual() {
        return elapsedTime().greaterThan(estimatedDuration());
    }

    @Override
    public boolean hasPreviousBuild() {
        return null != build.getPreviousBuild();
    }

    @Override
    public BuildViewModel previousBuild() {
        return new BuildView(build.getPreviousBuild(), augmentor, systemTime);
    }

    @Override
    public Set<User> culprits() {
        Set<User> culprits = new HashSet<User>();

        if (build instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> jenkinsBuild = (AbstractBuild<?, ?>) build;

            if (! (isRunning(jenkinsBuild))) {
                for (hudson.model.User culprit : jenkinsBuild.getCulprits()) {
                    culprits.add(new User(culprit.getFullName(), Jenkins.getInstance() != null ? Functions.getAvatar(culprit, "48x48") : null));
                }
            }
        }

        return culprits;
    }

    @Override
    public boolean isClaimed() {
        return claim().wasMade();
    }

    @Override
    public String claimant() {
        return claim().author();
    }

    @Override
    public String reasonForClaim() {
        return claim().reason();
    }

    private Claim claim() {
        return augmentor.detailsOf(build, Claim.class);
    }

    @Override
    public boolean hasKnownFailures() {
        return analysis().foundKnownFailures();
    }

    @Override
    public List<String> knownFailures() {
        return  analysis().failures();
    }

    private Analysis analysis() {
        return augmentor.detailsOf(build, Analysis.class);
    }

    public String toString() {
        return name();
    }


    private long now() {
        return systemTime.getTime();
    }

    private long whenTheBuildStarted() {
        return build.getTimestamp().getTimeInMillis();
    }


    private BuildView(Run<?, ?> build, BuildAugmentor augmentor, Date systemTime) {
        this.build = build;
        this.systemTime = systemTime;
        this.augmentor = augmentor;
    }
}
