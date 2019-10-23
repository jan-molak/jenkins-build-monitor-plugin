package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;

import org.codehaus.jackson.annotate.JsonProperty;

public class HasConfig implements Feature<HasConfig.Config> {
    private final com.smartcodeltd.jenkinsci.plugins.buildmonitor.Config config;

    public HasConfig(com.smartcodeltd.jenkinsci.plugins.buildmonitor.Config config) {
        this.config = config;
    }

    @Override
    public HasConfig of(JobView jobView) {
        return this;
    }

    @Override
    public Config asJson() {
        return new Config(config);
    }

    public static class Config {
        private final com.smartcodeltd.jenkinsci.plugins.buildmonitor.Config config;

        public Config(com.smartcodeltd.jenkinsci.plugins.buildmonitor.Config config) {
            this.config = config;
        }

        @JsonProperty
        public final String displayBadges() {
            return config.getDisplayBadges().name();
        }
    }

}
