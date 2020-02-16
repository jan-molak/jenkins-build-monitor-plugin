package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features;

import org.codehaus.jackson.annotate.JsonValue;

/**
 * @author Vincent & Robert
 */
public class UpStreamError {
    private final String value;

    public UpStreamError(String value) {
        this.value = value;
    }
    @JsonValue
    public String value() {
        return value;
    }

    public UpStreamError asJson() {
        return this;
    }
}