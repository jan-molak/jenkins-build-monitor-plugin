package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.User;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.bfa.Analysed;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.bfa.Analysis;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.bfa.NotAnalysed;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.claim.Claim;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.claim.Claimed;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.claim.NotClaimed;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.BuildStateRecipe;

import hudson.model.Result;
import hudson.model.Run;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BuildAugmentorTest {

    private static final String AUTHOR = "Adam";
    private static final String REASON = "Sorry, I broke it, fixing now";
    private static final String FLUX_CAPACITOR_FAILED_AGAIN = "Flux capacitor failed again";

    private BuildAugmentor augmentor = new BuildAugmentor();

    private Run<?, ?> plainBuild = a(build());
    private Run<?, ?> claimedBuild = a(build().finishedWith(Result.FAILURE).andWasClaimedBy(new User(AUTHOR), REASON));
    private Run<?, ?> failedBuild = a(build().finishedWith(Result.FAILURE).andKnownFailures(FLUX_CAPACITOR_FAILED_AGAIN));

    /*
     * Claim tests
     */

    @Test
    public void should_recognise_any_build_as_not_claimed_by_default() {
        assertThat(augmentor.detailsOf(plainBuild, Claim.class),     instanceOf(NotClaimed.class));
        assertThat(augmentor.detailsOf(claimedBuild, Claim.class),   instanceOf(NotClaimed.class));
    }

    @Test
    public void should_recognise_a_claimed_build_once_you_ask_him_to_do_it() throws Exception {
        augmentor.support(Claim.class);

        Claim claim = augmentor.detailsOf(claimedBuild, Claim.class);

        assertThat(claim, instanceOf(Claimed.class));

        assertThat(claim.wasMade(), is(true));
        assertThat(claim.author(),  is(AUTHOR));
        assertThat(claim.reason(),  is(REASON));
    }

    /*
     * Analysis tests
     */
    @Test
    public void should_recognise_any_build_as_not_analysed_by_default() {
        assertThat(augmentor.detailsOf(plainBuild, Analysis.class),    instanceOf(NotAnalysed.class));
        assertThat(augmentor.detailsOf(failedBuild, Analysis.class),   instanceOf(NotAnalysed.class));
    }

    @Test
    public void should_recognise_a_failed_build_once_you_ask_him_to_do_it() throws Exception {
        augmentor.support(Analysis.class);

        Analysis analysis = augmentor.detailsOf(failedBuild, Analysis.class);

        assertThat(analysis, instanceOf(Analysed.class));

        assertThat(analysis.foundKnownFailures(), is(true));
        assertThat(analysis.failures(),  Matchers.contains(FLUX_CAPACITOR_FAILED_AGAIN));
    }

    /*
     * Syntactic sugar
     */

    private Run<?, ?> a(BuildStateRecipe recipe) {
        return recipe.execute();
    }

    private BuildStateRecipe build() {
        return new BuildStateRecipe();
    }
}