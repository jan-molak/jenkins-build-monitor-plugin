package com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.prerequisites;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.scenarios.Context;

public interface Prerequisite {
    Context accept(Context context) throws Exception;
}
