package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar;

import com.google.common.base.Supplier;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.facade.RelativeLocation;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features.Feature;
import hudson.model.Job;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class JobViewRecipe implements Supplier<JobView> {
    private Job<?, ?> job;
    private RelativeLocation relative;
    private Date systemTime = new Date();
    private List<Feature> features = newArrayList();
    private boolean isPipeline;

    public JobViewRecipe of(Job<?, ?> job) {
        this.job = job;
        this.relative = RelativeLocation.of(job); // default
        return this;
    }

    public JobViewRecipe which(Feature... features) {
        this.features = Arrays.asList(features);
        return this;
    }

    public JobViewRecipe isAPipeline() {
        this.isPipeline = true;
        return this;
    }

    // todo: should RelativeLocation be a Feature?
    public JobViewRecipe with(RelativeLocation relative) {
        this.relative = relative;
        return this;
    }

    public JobViewRecipe assuming(Date systemTime) {
        this.systemTime = systemTime;
        return this;
    }

    @Override
    public JobView get() {
        return new JobView(job, features, isPipeline, relative, systemTime, false);
    }
}
