package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.headline.HeadlineConfig;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.Sugar.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class})
public class HasHeadlineShowingFailedBuildDetailsTest {

    private JobView view;

    @Mock
    private Jenkins jenkins;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
    }

    @Test
    public void should_tell_who_broke_the_build() throws Exception {
        view = a(jobView().which(hasHeadlineThatShowsCommitters()).of(
                a(job().whereTheLast(build().wasBrokenBy("Adam")))));

        assertThat(headlineOf(view), is("1 build has failed since Adam committed their changes"));
    }

    @Test
    public void should_list_committers_who_broke_the_build_in_alphabetical_order() throws Exception {
        view = a(jobView().which(hasHeadlineThatShowsCommitters()).of(
                a(job().whereTheLast(build().wasBrokenBy("Adam", "Ben")))));

        assertThat(headlineOf(view), is("1 build has failed since Adam and Ben committed their changes"));
    }

    @Test
    public void should_tell_the_number_of_broken_builds_since_the_last_broken_build_and_the_author_of_the_first_offending_commit() throws Exception {
        view = a(jobView().which(hasHeadlineThatShowsCommitters()).of(
                a(job().whereTheLast(build().wasBrokenBy("Adam")).
                        andThePrevious(build().wasBrokenBy("Ben", "Connor")).
                        andThePrevious(build().wasBrokenBy("Daniel")).
                        andThePrevious(build().succeededThanksTo("Errol")))));

        assertThat(headlineOf(view), is("3 builds have failed since Daniel committed their changes"));
    }

    @Test
    public void should_tell_the_number_of_broken_builds_since_the_last_build_broken_by_multiple_committers() throws Exception {
        view = a(jobView().which(hasHeadlineThatShowsCommitters()).of(
                a(job().whereTheLast(build().wasBrokenBy("Adam")).
                        andThePrevious(build().wasBrokenBy("Daniel", "Ben", "Connor")).
                        andThePrevious(build().succeededThanksTo("Errol")))));

        assertThat(headlineOf(view), is("2 builds have failed since Ben, Connor and Daniel committed their changes"));
    }

    @Test
    public void should_tell_how_many_builds_have_failed_but_not_who_broke_them_if_configured_as_such() throws Exception {
        view = a(jobView().which(hasHeadlineThatDoesNotShowCommitters()).of(
                a(job().whereTheLast(build().wasBrokenBy("Adam")))));

        assertThat(headlineOf(view), is("1 build has failed"));
    }

    @Test
    public void should_tell_the_number_of_broken_builds_since_the_last_broken_build() throws Exception {
        view = a(jobView().which(hasHeadlineThatDoesNotShowCommitters()).of(
                a(job().whereTheLast(build().wasBrokenBy("Adam")).
                        andThePrevious(build().wasBrokenBy("Ben", "Connor")).
                        andThePrevious(build().wasBrokenBy("Daniel")).
                        andThePrevious(build().succeededThanksTo("Errol")))));

        assertThat(headlineOf(view), is("3 builds have failed"));
    }

    @Test
    public void should_tell_the_number_of_broken_builds_since_the_last_build_broken_when_multiple_committers_are_involver() throws Exception {
        view = a(jobView().which(hasHeadlineThatDoesNotShowCommitters()).of(
                a(job().whereTheLast(build().wasBrokenBy("Adam")).
                        andThePrevious(build().wasBrokenBy("Daniel", "Ben", "Connor")).
                        andThePrevious(build().succeededThanksTo("Errol")))));

        assertThat(headlineOf(view), is("2 builds have failed"));
    }

    // --

    private Feature hasHeadlineThatShowsCommitters() {
        return new HasHeadline(new HeadlineConfig(true, false));
    }

    private Feature hasHeadlineThatDoesNotShowCommitters() {
        return new HasHeadline(new HeadlineConfig(false, false));
    }

    private String headlineOf(JobView job) {
        return job.which(HasHeadline.class).asJson().value();
    }
}
