package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar;

import com.google.common.base.Supplier;
import hudson.model.AbstractBuild;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.ViewJob;

import java.util.Stack;

import static org.mockito.Mockito.*;

/**
 * @author Jan Molak
 */
public class JobStateRecipe implements Supplier<Job<?,?>> {
    private Job<?, ?> job;
    private Stack<AbstractBuild> buildHistory = new Stack<AbstractBuild>();

    public JobStateRecipe() {
        job = mock(Job.class);

        when(job.isBuildable()).thenReturn(Boolean.TRUE);
    }

    public JobStateRecipe withName(String name) {
        when(job.getName()).thenReturn(name);

        // The name of the job also defines its URL, that's why the stub for getUrl() is defined here as well.
        // You could argue, that 'withUrl' could be a separate method on the builder,
        // but then this would allow for creation of impossible scenarios, such as:
        // job.withName("a-name").withUrl("something completely different"), which leads nowhere.
        return withShortUrl(name);
    }

    private JobStateRecipe withShortUrl(String url) {
        when(job.getShortUrl()).thenReturn(url);

        // This might not necessarily belong here,
        // but I don't need to introduce the concept of a parent anywhere else yet.
        ItemGroup parent = mock(ItemGroup.class);
        when(parent.getUrl()).thenReturn("job/");
        when(job.getParent()).thenReturn(parent);

        return this;
    }

    public JobStateRecipe withDisplayName(String name) {
        when(job.getDisplayNameOrNull()).thenReturn(name);
        when(job.getDisplayName()).thenReturn(name);

        return this;
    }

    public JobStateRecipe thatHasNeverRun() {
        buildHistory.clear();

        return this;
    }

    public JobStateRecipe thatIsNotBuildable() {
        when(job.isBuildable()).thenReturn(Boolean.FALSE);

        return this;
    }

    public JobStateRecipe thatIsAnExternalJob() {
        job = mock(ViewJob.class);

        when(job.isBuildable()).thenReturn(Boolean.FALSE);

        return this;
    }

    public JobStateRecipe whereTheLast(BuildStateRecipe recipe) {
        return updatedWithOnlyOneHistoryEntryFor(recipe.get());
    }

    public JobStateRecipe andThePrevious(BuildStateRecipe recipe) {
        return updatedWithEarliestHistoryEntryFor(recipe.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Job<?, ?> get() {
        AbstractBuild earlierBuild, earliestBuild;

        // link "previous" builds ...
        while (buildHistory.size() > 1) {
            earliestBuild = buildHistory.pop();
            earlierBuild  = buildHistory.peek();

            when(earlierBuild.getPreviousBuild()).thenReturn(earliestBuild);
        }

        // pick the first build from the build history and make it the "last build"
        if (buildHistory.size() == 1) {
        	doReturn(buildHistory.pop()).when(job).getLastBuild();
        }

        return job;
    }

    private JobStateRecipe updatedWithEarliestHistoryEntryFor(AbstractBuild build) {
        buildHistory.push(build);

        return this;
    }

    private JobStateRecipe updatedWithOnlyOneHistoryEntryFor(AbstractBuild build) {
        buildHistory.clear();

        return updatedWithEarliestHistoryEntryFor(build);
    }
}