package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;
import hudson.model.AbstractProject;

import java.util.List;

/**
 * Created by vincent on 2017-04-28.
 */

public class showIfUpstreamProjectFails implements Feature<UpStreamError> {
    private List<AbstractProject<?, ?>> project;

    private JobView job;

    public showIfUpstreamProjectFails(List<AbstractProject<?, ?>> project) {
        this.project = project;
    }

    public UpStreamError asJson() {
        return new UpStreamError(checkAllUpstreamProjects(project));
    }

    @Override
    public showIfUpstreamProjectFails of(JobView jobView) {
        this.job = jobView;
        return this;
    }

    private String RecursiveUpstreamCheck(List<AbstractProject> upstreamProjects) {
        for (AbstractProject upstreamProject : upstreamProjects) {
            if (!upstreamProject.getUpstreamProjects().isEmpty()) {
                return RecursiveUpstreamCheck(upstreamProject.getUpstreamProjects());
            }
            if (upstreamProject.isBuilding()) {
                return "";
            } else if (upstreamProject.isDisabled()) {
                return "project has build in chain that is disabled. name: '"+upstreamProject.getName()+"'";
            } else if (upstreamProject.isBuildBlocked()) {
                return "Project can't run due to project with name: '"+upstreamProject.getName()+"' in upstream chain being blocked, Reason: " + upstreamProject.getWhyBlocked();
            }
            //because jenkins doesn't natively have a way to check if a build has failed so I have improvised.
            else if (upstreamProject.getBuildStatusUrl().equals("red.png")) {
                return "project in upstream chain has failed, check Build: '" + upstreamProject.getName() + "'";
            }
        }
        return "";
    }

    private String checkAllUpstreamProjects(List<AbstractProject<?, ?>> project) {
        String checkRed = "";
        for (AbstractProject<?, ?> aProject : project) {
            if (!aProject.getUpstreamProjects().isEmpty() && aProject.getFullName().equals(job.name())) {
                //getUpstreamProjects happened to not check them recursively, so I made my own.
                List<AbstractProject> ups = aProject.getUpstreamProjects();
                checkRed = RecursiveUpstreamCheck(ups);
            }
        }
        return checkRed;
    }
}
