package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.facade.StaticJenkinsAPIs;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.*;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.headline.HeadlineConfig;
import hudson.model.Job;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Jan Molak
 */
public class JobViews {
    private static final String Claim = "claim";
    private static final String Build_Failure_Analyzer = "build-failure-analyzer";
    private static final String Pipeline_Plugin = "workflow-aggregator";

    private final StaticJenkinsAPIs jenkins;
    private final com.smartcodeltd.jenkinsci.plugins.buildmonitor.Config config;

    public JobViews(StaticJenkinsAPIs jenkins, com.smartcodeltd.jenkinsci.plugins.buildmonitor.Config config) {
        this.jenkins = jenkins;
        this.config  = config;
    }

    public JobView viewOf(Job<?, ?> job) {
        List<Feature> viewFeatures = newArrayList();

        // todo: a more elegant way of assembling the features would be nice
        viewFeatures.add(new HasHeadline(new HeadlineConfig(config.shouldDisplayCommitters())));
        viewFeatures.add(new KnowsLastBuildDetails());

        if (jenkins.hasPlugin(Claim)) {
            viewFeatures.add(new CanBeClaimed());
        }

        if (jenkins.hasPlugin(Build_Failure_Analyzer)) {
            viewFeatures.add(new CanBeDiagnosedForProblems());
        }

        if (jenkins.hasPlugin(Pipeline_Plugin)) {
            viewFeatures.add(new HasPipeline());
        }

        return JobView.of(job, viewFeatures);
    }
}
