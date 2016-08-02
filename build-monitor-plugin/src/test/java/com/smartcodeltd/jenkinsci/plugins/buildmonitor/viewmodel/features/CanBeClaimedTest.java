package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.Sugar.*;
import static hudson.model.Result.FAILURE;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CanBeClaimedTest {
    private JobView job;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void should_know_if_a_failing_build_has_been_claimed() throws Exception {
        String ourPotentialHero = "Adam",
                theReason = "I broke it, sorry, fixing now";

        job = a(jobView().which(new CanBeClaimed()).of(
                a(job().whereTheLast(build().finishedWith(FAILURE).and().wasClaimedBy(ourPotentialHero, theReason)))));

        assertThat(serialisedClaimOf(job).author(), is(ourPotentialHero));
        assertThat(serialisedClaimOf(job).reason(), is(theReason));
    }

    @Test
    public void should_know_if_a_failing_build_has_not_been_claimed() throws Exception {
        job = a(jobView().which(new CanBeClaimed()).of(
                a(job().whereTheLast(build().finishedWith(FAILURE)))));

        assertThat(serialisedClaimOf(job), is(nullValue()));
    }

    @Test
    public void should_complain_if_the_build_was_not_claimable() throws Exception {
        job = a(jobView().of(
                a(job().withName("my-project").whereTheLast(build().finishedWith(FAILURE)))));

        thrown.expectMessage("CanBeClaimed is not a feature of this project: 'my-project'");

        job.which(CanBeClaimed.class);
    }

    private CanBeClaimed.Claim serialisedClaimOf(JobView job) {
        return job.which(CanBeClaimed.class).asJson();
    }
}
