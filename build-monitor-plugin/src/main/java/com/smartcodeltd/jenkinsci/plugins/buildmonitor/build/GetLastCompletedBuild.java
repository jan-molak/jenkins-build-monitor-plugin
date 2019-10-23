package com.smartcodeltd.jenkinsci.plugins.buildmonitor.build;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.BuildViewModel;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;

public class GetLastCompletedBuild implements GetBuildViewModel {

    @Override
    public BuildViewModel from(JobView job) {
        return job.lastCompletedBuild();
    }

}
