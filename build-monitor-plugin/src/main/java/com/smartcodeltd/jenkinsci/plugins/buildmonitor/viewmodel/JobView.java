package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.facade.RelativeLocation;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.duration.Duration;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.Feature;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.RunList;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

/**
 * @author Jan Molak
 */
@JsonSerialize(using = JobViewSerialiser.class)
public class JobView {
    private final Date systemTime;
    private final Job<?, ?> job;
    private final boolean isPipelineJob;
    private final RelativeLocation relative;
    private final boolean showShortenedJobName;

    private final List<Feature> features = newArrayList();

    public static JobView of(Job<?, ?> job, List<Feature> features, boolean isPipelineJob, boolean showShortenedJobName) {
        return new JobView(job, features, isPipelineJob, RelativeLocation.of(job), new Date(), showShortenedJobName);
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "systemTime is non-critical and no security should be compromised by mutating")
    public JobView(Job<?, ?> job, List<Feature> features, boolean isPipelineJob, RelativeLocation relative, Date systemTime,
                   boolean showShortenedJobName) {
        this.job           = job;
        this.isPipelineJob = isPipelineJob;
        this.relative      = relative;
        this.systemTime    = systemTime;
        this.showShortenedJobName = showShortenedJobName;

        for (Feature feature : features) {
            this.features.add(feature.of(this));
        }
    }

    public List<Feature> features() {
        return ImmutableList.copyOf(features);
    }

    public <F extends Feature> F which(Class<F> requestedFeature) {
        for (Feature feature : features) {
            if (feature.getClass() == requestedFeature) {
                return (F) feature;
            }
        }

        throw new RuntimeException(format("%s is not a feature of this project: '%s'", requestedFeature.getSimpleName(), job.getName()));
    }

    public String name() {
        String name = relative.name();
        if (showShortenedJobName) {
            name = name.substring(name.indexOf('»') + 1);
        }
        return name;
    }

    public String url() {
        return relative.url();
    }

    public String status() {
        return CssStatus.of(this);
    }

    public String estimatedDuration() {
        return formatted(lastBuild().estimatedDuration());
    }

    public String timeElapsedSinceLastBuild() {
        return formatted(lastCompletedBuild().timeElapsedSince());
    }

    private String formatted(Duration duration) {
        return null != duration
                ? duration.value()
                : "";
    }

    public int progress() {
        return lastBuild().progress();
    }

    public boolean isDisabled() {
        return ! (isJobBuildable() || isJobExternal());
    }

    private boolean isJobBuildable() {
        return job.isBuildable();
    }

    private boolean isJobExternal() {
        return job instanceof hudson.model.ViewJob;
    }

    public boolean isRunning() {
        return lastBuild().isRunning();
    }

    public Result lastResult() {
        return lastCompletedBuild().result();
    }

    // todo track by job.hashCode messes up the animation
    public int hashCode() {
        return Objects.hashCode(job.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equal(job, obj);
    }

    @Override
    public String toString() {
        return name();
    }

    // --

    @SuppressWarnings("unchecked")
	public List<BuildViewModel> currentBuilds() {
    	List<BuildViewModel> currentBuilds = newArrayList();
    	
    	RunList<Run<?, ?>> runList = ((RunList<Run<?, ?>>)job.getNewBuilds()).filter(BuildingPredicate.INSTANCE);

    	for (Iterator<Run<?, ?>> i = runList.iterator(); i.hasNext(); ) {
    		currentBuilds.add(buildViewOf(i.next()));
    	}
    	
        return currentBuilds;
    }

    public BuildViewModel lastBuild() {
        return buildViewOf(job.getLastBuild());
    }

    public BuildViewModel lastCompletedBuild() {
        BuildViewModel previousBuild = lastBuild();
        while (previousBuild.isRunning() && previousBuild.hasPreviousBuild()) {
            previousBuild = previousBuild.previousBuild();
        }

        return previousBuild;
    }

    private BuildViewModel buildViewOf(Run<?, ?> build) {
        if (null == build) {
            return new NullBuildView();
        }

        return BuildView.of(build, isPipelineJob, relative, systemTime);
    }
}
