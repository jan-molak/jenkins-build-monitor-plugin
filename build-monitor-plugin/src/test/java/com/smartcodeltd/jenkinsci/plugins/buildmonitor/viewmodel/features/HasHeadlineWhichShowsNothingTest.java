package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.headline.HeadlineConfig;
import org.junit.Test;

import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.Sugar.*;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

public class HasHeadlineWhichShowsNothingTest {
    private JobView view;

    @Test
    public void should_say_nothing_if_no_builds_were_executed() throws Exception {
        view = a(jobView().which(hasHeadlineThatShowsCommitters()).of(
                a(job().thatHasNeverRun())));

        assertThat(headlineOf(view), isEmptyString());
    }

    // --

    private Feature hasHeadlineThatShowsCommitters() {
        return new HasHeadline(new HeadlineConfig(true, false));
    }

    private String headlineOf(JobView job) {
        return job.which(HasHeadline.class).asJson().value();
    }
}
