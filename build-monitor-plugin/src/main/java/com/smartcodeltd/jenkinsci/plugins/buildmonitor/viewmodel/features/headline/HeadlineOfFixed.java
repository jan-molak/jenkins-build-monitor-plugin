package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.headline;

import com.google.common.collect.Sets;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.readability.Lister;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.BuildViewModel;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;

import java.util.Set;

import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static hudson.model.Result.*;

public class HeadlineOfFixed implements CandidateHeadline {

    private final JobView job;
    private final HeadlineConfig config;

    public HeadlineOfFixed(JobView job, HeadlineConfig config) {
        this.job = job;
        this.config = config;
    }

    @Override
    public boolean isApplicableTo(JobView job) {
        return didTheJobJustGetFixedWith(job.lastCompletedBuild()) && config.displayCommittersOnFixedBuild;
    }

    @Override
    public Headline asJson() {
        return new Headline(textFor(job.lastCompletedBuild()));
    }

    private String textFor(BuildViewModel lastBuild) {

        return Lister.describe(
                "",
                "Fixed after %s committed their changes :-)",
                newLinkedList(committersOf(lastBuild))
        );
    }

    private boolean didTheJobJustGetFixedWith(BuildViewModel build) {
        return SUCCESS.equals(build.result()) && previousFailed(build);
    }

    private boolean previousFailed(BuildViewModel build) {
        return build.hasPreviousBuild() &&
                contains(newArrayList(FAILURE, UNSTABLE, ABORTED), build.previousBuild().result());
    }

    private Set<String> committersOf(BuildViewModel build) {
        return config.displayCommittersOnFixedBuild
                ? build.committers()
                : Sets.<String>newHashSet();
    }
}
