package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.BuildViewModel;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;
import org.jenkinsci.plugins.junitrealtimetestreporter.AbstractRealtimeTestResultAction;
import org.jenkinsci.plugins.junitrealtimetestreporter.TestProgress;

/**
 * @author Daniel Beland
 */
public class HasJunitRealtime implements Feature<HasJunitRealtime.RealtimeTests> {
	private ActionFilter filter = new ActionFilter();
	private JobView job;

	public HasJunitRealtime() {
	}

	@Override
	public HasJunitRealtime of(JobView jobView) {
		this.job = jobView;

		return this;
	}

	@Override
	public RealtimeTests asJson() {
		List<BuildViewModel> builds = job.currentBuilds();
		if (builds.isEmpty()) {
			return null;
		}

		BuildViewModel build = builds.get(0);
		Iterator<AbstractRealtimeTestResultAction> details = Iterables
				.filter(build.allDetailsOf(AbstractRealtimeTestResultAction.class), filter).iterator();

		return details.hasNext() ? new RealtimeTests(details) : null; // `null` because we don't want to serialise an
																		// empty object
	}

	public static class RealtimeTests {
		private final List<RealtimeTest> realtimeTests = newArrayList();

		public RealtimeTests(Iterator<AbstractRealtimeTestResultAction> realtimeTestResultAction) {
			while (realtimeTestResultAction.hasNext()) {
				realtimeTests.add(new RealtimeTest(realtimeTestResultAction.next().getTestProgress()));
			}
		}

		@JsonValue
		public List<RealtimeTest> value() {
			return ImmutableList.copyOf(realtimeTests);
		}
	}

	public static class RealtimeTest {
		private final TestProgress testProgress;

		public RealtimeTest(TestProgress testProgress) {
			this.testProgress = testProgress;
		}

		@JsonProperty
		public String getEstimatedRemainingTime() {
			return testProgress.getEstimatedRemainingTime();
		}

		@JsonProperty
		public int[] getCompletedPercentages() {
			int value1 = Math.min(testProgress.getCompletedTestsPercentage(), testProgress.getCompletedTimePercentage());
			int value2 = Math.max(testProgress.getCompletedTestsPercentage(), testProgress.getCompletedTimePercentage());

			return new int[] { value1, value2 - value1};
		}

		@JsonProperty
		public int getCompletedTests() {
			return testProgress.getCompletedTests();
		}

		@JsonProperty
		public int getExpectedTests() {
			return testProgress.getExpectedTests();
		}

		@JsonProperty
		public String getStyle() {
			return testProgress.getStyle();
		}
	}

	private static class ActionFilter implements Predicate<AbstractRealtimeTestResultAction> {
		@Override
		public boolean apply(AbstractRealtimeTestResultAction action) {
			// Need to trigger the polling manually first
			action.getResult();
			return action.getTestProgress() != null;
		}
	}

}
