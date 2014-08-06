package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.facade.RelativeLocation;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.Augmentation;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.BuildAugmentor;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.bfa.Analysis;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.claim.Claim;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.BuildStateRecipe;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.JobStateRecipe;
import hudson.model.Job;
import hudson.model.Result;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.Loops.asFollows;
import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.TimeMachine.assumeThat;
import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.TimeMachine.assumeThatCurrentTime;
import static hudson.model.Result.*;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Jan Molak
 */
public class JobViewTest {

    private static final String theName     = "some-TLAs-followed-by-a-project-name";
    private static final String displayName = "Pretty name that has some actual meaning";

    private RelativeLocation relative = mock(RelativeLocation.class);
    private JobView view;

    /*
     * By the way, if you were not aware of this: the configuration page of each job has an "Advanced Project Options"
     * section, where you can set a user-friendly "Display Name"
     */
    @Test
    public void delegates_the_process_of_determining_the_relative_job_name() {
        when(relative.name()).thenReturn(theName);

        view = JobView.of(a(job().withName(theName)), relative);

        assertThat(view.name(), is(theName));
        assertThat(view.toString(), is(theName));
        verify(relative, times(2)).name();
    }

    @Test
    public void delegates_the_process_of_determining_the_relative_url() {
        String expectedUrl = "job/" + theName;
        when(relative.url()).thenReturn(expectedUrl);

        view = JobView.of(a(job().withName(theName).withDisplayName(displayName)), relative);

        assertThat(view.url(), is(expectedUrl));
        verify(relative, times(1)).url();
    }

    @Test
    public void should_know_current_build_number() {
        view = JobView.of(a(job().whereTheLast(build().numberIs(5))));

        assertThat(view.lastBuildName(), is("#5"));
    }

    @Test
    public void should_use_build_name_if_its_known() {
        view = JobView.of(a(job().whereTheLast(build().nameIs("1.3.4+build.15"))));

        assertThat(view.lastBuildName(), is("1.3.4+build.15"));
    }

    @Test
    public void should_know_the_url_of_the_build() {
        // setting url on the stub is far from ideal, but hudson.model.Run is not particularly easy to test ...
        view = JobView.of(a(job().whereTheLast(build().urlIs("job/project-name/22/"))));

        assertThat(view.lastBuildUrl(), is("job/project-name/22/"));
    }

    /*
     * Should be able to measure the progress
     */

    @Test
    public void progress_of_a_not_started_job_should_be_zero() {
        view = JobView.of(a(job()));

        assertThat(view.progress(), is(0));
    }

    @Test
    public void progress_of_a_finished_job_should_be_zero() {
        view = JobView.of(a(job().whereTheLast(build().finishedWith(SUCCESS))));

        assertThat(view.progress(), is(0));
    }

    @Test
    public void progress_of_a_nearly_finished_job_should_be_100() throws Exception {
        view = JobView.of(
                    a(job().whereTheLast(build().isStillBuilding().startedAt("12:00:00").andUsuallyTakes(0))),
                    assumingThatCurrentTimeIs("12:00:00")
        );

        assertThat(view.progress(), is(100));
    }

    @Test
    public void progress_of_a_job_thats_taking_longer_than_expected_should_be_100() throws Exception {
        view = JobView.of(
                a(job().whereTheLast(build().isStillBuilding().startedAt("12:00:00").andUsuallyTakes(5))),
                assumingThatCurrentTimeIs("12:20:00")
        );

        assertThat(view.progress(), is(100));
    }

    @Test
    public void should_calculate_the_progress_of_a_running_job() throws Exception {
        view = JobView.of(
                a(job().whereTheLast(build().isStillBuilding().startedAt("13:10:00").andUsuallyTakes(5))),
                assumingThatCurrentTimeIs("13:11:00")
        );

        assertThat(view.progress(), is(20));
    }

    /*
     * Elapsed time
     */

    @Test
    public void should_know_how_long_a_build_has_been_running_for() throws Exception {

        String startTime              = "13:10:00",
               sixSecondsLater        = "13:10:06",
               twoAndHalfMinutesLater = "13:12:30",
               anHourAndHalfLater     = "14:40:00";
        Date   currentTime = assumeThatCurrentTime().is(startTime);

        view = JobView.of(
                a(job().whereTheLast(build().startedAt(startTime).isStillBuilding())),
                currentTime
        );

        assumeThat(currentTime).is(sixSecondsLater);
        assertThat(view.lastBuildDuration(), is("6s"));

        assumeThat(currentTime).is(twoAndHalfMinutesLater);
        assertThat(view.lastBuildDuration(), is("2m 30s"));

        assumeThat(currentTime).is(anHourAndHalfLater);
        assertThat(view.lastBuildDuration(), is("1h 30m 0s"));
    }

    @Test
    public void should_know_how_long_the_last_build_took_once_its_finished() throws Exception {
        view = JobView.of(a(job().whereTheLast(build().finishedWith(SUCCESS).andTook(3))));

        assertThat(view.lastBuildDuration(), is("3m 0s"));
    }

    @Test
    public void should_not_say_anything_about_the_duration_if_the_build_hasnt_run_yet() throws Exception {
        view = JobView.of(a(job()));

        assertThat(view.lastBuildDuration(), is(""));
    }

    @Test
    public void should_know_how_long_the_next_build_is_supposed_to_take() throws Exception {
        view = JobView.of(a(job().whereTheLast(build().finishedWith(SUCCESS).andUsuallyTakes(5))));

        assertThat(view.estimatedDuration(), is("5m 0s"));
    }

    @Test
    public void should_not_say_anything_if_it_doesnt_know_how_long_the_next_build_is_supposed_to_take() throws Exception {
        view = JobView.of(a(job()));

        assertThat(view.estimatedDuration(), is(""));
    }
    
    /*
     * Should produce a meaningful status description that can be used in the CSS
     */

    @Test
    public void should_describe_the_job_as_successful_if_the_last_build_succeeded() {
        view = JobView.of(a(job().whereTheLast(build().finishedWith(SUCCESS))));

        assertThat(view.status(), containsString("successful"));
    }

    @Test
    public void should_describe_the_job_as_failing_if_the_last_build_failed() {
        for (Result result : asFollows(FAILURE, ABORTED, NOT_BUILT, UNSTABLE)) {
            view = JobView.of(a(job().whereTheLast(build().finishedWith(result))));

            assertThat(view.status(), containsString("failing"));
        }
    }

    @Test
    public void should_describe_the_job_as_running_if_it_is_running() {
        List<JobView> views = asFollows(
                JobView.of(a(job().whereTheLast(build().hasntStartedYet()))),
                JobView.of(a(job().whereTheLast(build().isStillBuilding()))),
                JobView.of(a(job().whereTheLast(build().isStillUpdatingTheLog())))
        );

        for (JobView view : views) {
            assertThat(view.status(), containsString("running"));
        }
    }

    @Test
    public void should_describe_the_job_as_running_and_successful_if_it_is_running_and_the_previous_build_succeeded() {
        List<JobView> views = asFollows(
                JobView.of(a(job().
                        whereTheLast(build().hasntStartedYet()).
                        andThePrevious(build().finishedWith(SUCCESS)))),

                JobView.of(a(job().
                        whereTheLast(build().isStillBuilding()).
                        andThePrevious(build().finishedWith(SUCCESS)))),

                JobView.of(a(job().
                        whereTheLast(build().isStillUpdatingTheLog()).
                        andThePrevious(build().finishedWith(SUCCESS))))
        );


        // I could do this instead of having two assertions:
        // assertThat(view.status(), both(containsString("successful")).and(containsString("running")));
        // but then it would require Java 7

        for (JobView view : views) {
            assertThat(view.status(), containsString("successful"));
            assertThat(view.status(), containsString("running"));
        }
    }

    @Test
    public void should_describe_the_job_as_running_and_failing_if_it_is_running_and_the_previous_build_failed() {
        List<JobView> views = asFollows(
                JobView.of(a(job().
                        whereTheLast(build().hasntStartedYet()).
                        andThePrevious(build().finishedWith(FAILURE)))),

                JobView.of(a(job().
                        whereTheLast(build().isStillBuilding()).
                        andThePrevious(build().finishedWith(FAILURE)))),

                JobView.of(a(job().
                        whereTheLast(build().isStillUpdatingTheLog()).
                        andThePrevious(build().finishedWith(FAILURE))))
        );

        for (JobView view : views) {
            assertThat(view.status(), containsString("failing"));
            assertThat(view.status(), containsString("running"));
        }
    }

    /*
     * Parallel build execution handling
     */

    @Test
    public void should_describe_the_job_as_successful_when_there_are_several_builds_running_in_parallel_and_the_last_completed_was_successful() {
        view = JobView.of(a(job().
                        whereTheLast(build().isStillBuilding()).
                        andThePrevious(build().isStillBuilding()).
                        andThePrevious(build().finishedWith(SUCCESS))));

        assertThat(view.status(), containsString("successful"));
    }

    @Test
    public void should_describe_the_job_as_failing_when_there_are_several_builds_running_in_parallel_and_the_last_completed_failed() {
        view = JobView.of(a(job().
                whereTheLast(build().isStillBuilding()).
                andThePrevious(build().isStillBuilding()).
                andThePrevious(build().finishedWith(FAILURE))));

        assertThat(view.status(), containsString("failing"));
    }

    @Test
    public void should_describe_the_job_as_claimed_if_someone_claimed_last_build_failures() {
        view = JobView.of(
                a(job().whereTheLast(build().finishedWith(FAILURE).andWasClaimedBy(new User("Adam"), "sorry, I broke it, fixing now"))),
                augmentedWith(Claim.class),
                new JobViewConfiguration()
               );

        assertThat(view.status(), containsString("claimed"));
    }

    /*
     * Should produce some basic build statistics
     */

    @Test
    @Ignore
    public void should_know_how_long_the_job_has_been_failing_for() {
        // TODO Implement missing feature
    }

    /*
     * Should know who broke the build
     */

    @Test
    public void should_know_who_broke_the_build() {
        view = JobView.of(a(job().whereTheLast(build().wasBrokenBy(new User("Adam"), new User("Ben")))));

        assertThat(view.culprits(), hasSize(2));
        assertThat(view.culprits(), hasItems(new User("Adam"), new User("Ben")));
    }

    @Test
    public void should_know_who_has_been_committing_over_broken_build() {
        view = JobView.of(a(job().
                whereTheLast(build().wasBrokenBy(new User("Adam"))).
                andThePrevious(build().wasBrokenBy(new User("Ben"), new User("Connor"))).
                andThePrevious(build().wasBrokenBy(new User("Daniel"))).
                andThePrevious(build().succeededThanksTo(new User("Errol")))));

        assertThat(view.culprits(), hasSize(4));
        assertThat(view.culprits(), hasItems(new User("Adam"), new User("Ben"), new User("Connor"), new User("Daniel")));
        assertThat(view.culprits(), not(hasItem(new User("Errol"))));
    }

    @Test
    public void should_only_mention_each_culprit_once() {
        view = JobView.of(a(job().
                whereTheLast(build().wasBrokenBy(new User("Adam"))).
                andThePrevious(build().wasBrokenBy(new User("Adam"), new User("Ben"))).
                andThePrevious(build().wasBrokenBy(new User("Ben"), new User("Connor")))));

        assertThat(view.culprits(), hasSize(3));
        assertThat(view.culprits(), hasItems(new User("Adam"), new User("Ben"), new User("Connor")));
    }

    @Test
    public void should_not_mention_any_culprits_if_the_build_was_successful() {
        view = JobView.of(a(job().whereTheLast(build().succeededThanksTo(new User("Adam")))));

        assertThat(view.culprits(), hasSize(0));
    }

    @Test
    public void should_not_mention_any_culprits_if_the_build_was_successful_and_is_still_running() {
        view = JobView.of(a(job().
                whereTheLast(build().isStillBuilding()).
                andThePrevious(build().succeededThanksTo(new User("Adam")))));

        assertThat(view.culprits(), hasSize(0));
    }

    @Test
    public void should_indicate_culprits_if_the_build_is_failing_and_not_claimed() {
        view = JobView.of(a(job().
                        whereTheLast(build().wasBrokenBy(new User("Adam")))),
                augmentedWith(Claim.class),
                new JobViewConfiguration());

        assertThat(view.shouldIndicateCulprits(), is(true));
        assertThat(view.culprits(), hasSize(1));
    }

    @Test
    public void should_not_indicate_any_culprits_if_the_build_was_failing_but_is_now_claimed() {
        view = JobView.of(a(job().
                whereTheLast(build().wasBrokenBy(new User("Adam")).andWasClaimedBy(new User("Ben"), "Helping out Adam"))),
                augmentedWith(Claim.class),
                new JobViewConfiguration());

        assertThat(view.shouldIndicateCulprits(), is(false));
        assertThat(view.culprits(), hasSize(1));
    }

    @Test
    @Ignore
    public void should_know_the_authors_of_commits_that_made_it_into_the_build() {
        //TODO implement shouldKnowTheAuthorsOfCommitsThatMadeItIntoTheBuild
//        List<JobView> views = asFollows(
//            JobView.of(a(job().whereTheLast(build().succeededThanksTo("Adam")))),
//            JobView.of(a(job().whereTheLast(build().wasBrokenBy("Adam"))))
//        );
//
//        for (JobView view : views) {
//            assertThat(view.authors(), hasSize(1));
//            assertThat(view.authors(), hasItems("Adam"));
//        }
    }

    /*
     * Should know who claimed a broken build
     */

    @Test
    public void should_know_if_a_failing_build_has_been_claimed() throws Exception {
        User ourPotentialHero = new User("Adam");
        String theReason = "I broke it, sorry, fixing now";

        view = JobView.of(
                a(job().whereTheLast(build().finishedWith(FAILURE).andWasClaimedBy(ourPotentialHero, theReason))),
                augmentedWith(Claim.class),
                new JobViewConfiguration()
        );

        assertThat(view.isClaimed(),      is(true));
        assertThat(view.claimAuthor(),       is(ourPotentialHero.getName()));
        assertThat(view.claimReason(), is(theReason));
    }

    @Test
    public void should_describe_known_failures() {
        String rogueAi = "Pod bay doors didn't open";

        view = JobView.of(
                a(job().whereTheLast(build().finishedWith(FAILURE).andKnownFailures(rogueAi))),
                augmentedWith(Analysis.class),
                new JobViewConfiguration());

        assertThat(view.hasKnownFailures(), is(true));
        assertThat(view.knownFailures(), IsCollectionContaining.hasItem(rogueAi));
    }

    @Test
    public void public_api_should_return_reasonable_defaults_for_jobs_that_never_run() throws Exception {
        view = JobView.of(a(job().thatHasNeverRun()));

        assertThat(view.lastBuildName(),          is(""));
        assertThat(view.lastBuildUrl(),           is(""));
        assertThat(view.lastBuildDuration(),      is(""));
        assertThat(view.estimatedDuration(),      is(""));
        assertThat(view.progress(),               is(0));
        assertThat(view.shouldIndicateCulprits(), is(false));
        assertThat(view.culprits(),               hasSize(0));
        assertThat(view.status(),                 is("failing"));
        assertThat(view.isClaimed(),              is(false));
        assertThat(view.hasKnownFailures(),       is(false));
    }

    /*
     * Syntactic sugar
     */

    private JobStateRecipe job() {
        return new JobStateRecipe();
    }

    private Job<?, ?> a(JobStateRecipe recipe) {
        return recipe.execute();
    }

    private BuildStateRecipe build() {
        return new BuildStateRecipe();
    }

    private Date assumingThatCurrentTimeIs(String currentTime) throws ParseException {
        Date currentDate = new SimpleDateFormat("H:m:s").parse(currentTime);

        Date systemTime = mock(Date.class);
        when(systemTime.getTime()).thenReturn(currentDate.getTime());

        return systemTime;
    }

    private RelativeLocation locatedAtTheTopLevel() {

        RelativeLocation location = mock(RelativeLocation.class);
        when(location.name()).thenReturn("a");
        when(location.url()).thenReturn("b");

        return location;
    }

    private BuildAugmentor augmentedWith(Class<? extends Augmentation>... augmentationsToSupport) {
        BuildAugmentor augmentor = new BuildAugmentor();

        for (Class<? extends Augmentation> augmentation : augmentationsToSupport) {
            augmentor.support(augmentation);
        }

        return augmentor;
    }    
}