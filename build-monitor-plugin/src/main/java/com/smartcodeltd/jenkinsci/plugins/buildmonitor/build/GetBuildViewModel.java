package com.smartcodeltd.jenkinsci.plugins.buildmonitor.build;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.BuildViewModel;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;

public interface GetBuildViewModel {

    BuildViewModel from(JobView job);
}
