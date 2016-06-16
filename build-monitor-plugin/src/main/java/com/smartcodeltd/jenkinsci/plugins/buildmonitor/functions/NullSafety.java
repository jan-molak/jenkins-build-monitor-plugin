package com.smartcodeltd.jenkinsci.plugins.buildmonitor.functions;

public class NullSafety {
    private NullSafety(){}

    /**
     * @param value         a value that can be a potential null
     * @param defaultValue  a default to be returned if the value is null
     *
     * @return either value or defaultValue
     */
    public static <T> T getOrElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
