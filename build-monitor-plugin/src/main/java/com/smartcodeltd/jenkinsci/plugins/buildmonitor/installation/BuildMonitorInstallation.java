package com.smartcodeltd.jenkinsci.plugins.buildmonitor.installation;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.facade.StaticJenkinsAPIs;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.hash.Hashing.sha256;

public class BuildMonitorInstallation {
    private static final String UNKNOWN = "unknown";

    private String anonymousCorrelationId = UNKNOWN;
    private final StaticJenkinsAPIs jenkins;

    public BuildMonitorInstallation() {
        this(new StaticJenkinsAPIs());
    }

    public BuildMonitorInstallation(StaticJenkinsAPIs jenkinsAPIs) {
        this.jenkins   = jenkinsAPIs;
    }

    /**
     * Used to make sure that the anonymous Build Monitor stats are not double-counted,
     * as in a typical setup there's one Jenkins, but multiple Build Monitors.
     * @return anonymous Correlation Id
     */
    public String anonymousCorrelationId() {
        // we only need to calculate this once
        if (UNKNOWN.equalsIgnoreCase(anonymousCorrelationId)) {
            anonymousCorrelationId = sha256().hashString(jenkins.encodedPublicKey(), UTF_8).toString();
        }

        return anonymousCorrelationId;
    }

    public int size() {
        // Jenkins caches this value already, no need to cache it again
        return jenkins.numberOfUsers();
    }

    public Audience audience() {
        return (jenkins.isDevelopmentMode() || buildMonitorVersion().contains("SNAPSHOT"))
                ? Audience.BuildMonitorDevelopers
                : Audience.EndUsers;
    }

    private static final BuildMonitorBuildProperties buildProperties = new BuildMonitorBuildProperties("build-monitor.properties");

    public String buildMonitorVersion() {
        return buildProperties.get("version");
    }
}
