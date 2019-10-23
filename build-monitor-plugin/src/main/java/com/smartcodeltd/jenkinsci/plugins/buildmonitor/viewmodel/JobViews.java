package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.Config.DisplayOptions;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.facade.StaticJenkinsAPIs;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.*;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.headline.HeadlineConfig;
import hudson.model.Job;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Jan Molak
 */
public class JobViews {
    private static final String Claim                       = "claim";
    private static final String Build_Failure_Analyzer      = "build-failure-analyzer";
    private static final String Groovy_Post_Build           = "groovy-postbuild";
    private static final String Badge_Plugin                = "badge";
    private static final String Pipeline                    = "workflow-aggregator";
    private static final String GroovyPostbuildActionClass  = "org.jvnet.hudson.plugins.groovypostbuild.GroovyPostbuildAction";
    private static final String Junit_Realtime              = "junit-realtime-test-reporter";

    private final StaticJenkinsAPIs jenkins;
    private final com.smartcodeltd.jenkinsci.plugins.buildmonitor.Config config;

    public JobViews(StaticJenkinsAPIs jenkins, com.smartcodeltd.jenkinsci.plugins.buildmonitor.Config config) {
        this.jenkins = jenkins;
        this.config  = config;
    }

    public JobView viewOf(Job<?, ?> job) {
        List<Feature> viewFeatures = newArrayList();

        // todo: a more elegant way of assembling the features would be nice
        viewFeatures.add(new HasConfig(config));
        viewFeatures.add(new HasHeadline(new HeadlineConfig(config.shouldDisplayCommitters())));
        viewFeatures.add(new KnowsLastCompletedBuildDetails());
        viewFeatures.add(new KnowsCurrentBuildsDetails());

        if (jenkins.hasPlugin(Claim)) {
            viewFeatures.add(new CanBeClaimed());
        }

        if (jenkins.hasPlugin(Build_Failure_Analyzer)) {
            viewFeatures.add(new CanBeDiagnosedForProblems(config.getBuildFailureAnalyzerDisplayedField()));
        }

        if (config.getDisplayBadges() != DisplayOptions.Never) {
            if (jenkins.hasPlugin(Badge_Plugin)) {
                viewFeatures.add(new HasBadgesBadgePlugin(config));
            } else if (jenkins.hasPlugin(Groovy_Post_Build) && hasGroovyPostbuildActionClass()) {
                viewFeatures.add(new HasBadgesGroovyPostbuildPlugin(config));
            }
        }

        if (config.shouldDisplayJUnitProgress() && jenkins.hasPlugin(Junit_Realtime)) {
        	viewFeatures.add(new HasJunitRealtime());
        }

        boolean isPipelineJob = jenkins.hasPlugin(Pipeline) && job instanceof WorkflowJob;

        return JobView.of(job, viewFeatures, isPipelineJob);
    }

    private boolean hasGroovyPostbuildActionClass() {
        try {
            Class.forName(GroovyPostbuildActionClass);
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }
}
