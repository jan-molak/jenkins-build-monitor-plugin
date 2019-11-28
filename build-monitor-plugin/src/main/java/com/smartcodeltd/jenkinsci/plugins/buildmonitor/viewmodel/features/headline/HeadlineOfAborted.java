package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.headline;

import com.google.common.base.Optional;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.BuildViewModel;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;
import hudson.model.Result;
import hudson.model.User;
import jenkins.model.CauseOfInterruption;
import jenkins.model.InterruptedBuildAction;

import static java.lang.String.format;

/**
 * @author Jan Molak
 */
public class HeadlineOfAborted implements CandidateHeadline {
    private final JobView job;
    private final HeadlineConfig config;

    public HeadlineOfAborted(JobView job, HeadlineConfig config) {
        this.job = job;
        this.config = config;
    }

    @Override
    public boolean isApplicableTo(JobView job) {
        return Result.ABORTED.equals(job.lastBuild().result());
    }

    @Override
    public Headline asJson() {
        return new Headline(text(job.lastBuild()));
    }

    private String text(BuildViewModel build) {
        Optional<InterruptedBuildAction> interruption = build.detailsOf(InterruptedBuildAction.class);

        if (config.displayCommittersOnBuildFailure && interruption.isPresent()) {

            Optional<String> username = userResponsibleFor(interruption.get());

            if (username.isPresent()) {
                return format("Execution aborted by %s", username.get());
            }
        }

        return "Execution aborted";
    }

    private Optional<String> userResponsibleFor(InterruptedBuildAction details) {

        if (details.getCauses().size() == 1) {
            CauseOfInterruption cause = details.getCauses().get(0);

            if (cause instanceof CauseOfInterruption.UserInterruption) {
                User user = ((CauseOfInterruption.UserInterruption) cause).getUser();

                return Optional.of(user.getFullName());
            }
        }

        return Optional.absent();
    }
}
