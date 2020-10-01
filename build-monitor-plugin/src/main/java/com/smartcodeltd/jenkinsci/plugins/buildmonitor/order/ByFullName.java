package com.smartcodeltd.jenkinsci.plugins.buildmonitor.order;

import hudson.model.Job;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ByFullName implements Comparator<Job<?, ?>>, Serializable {

    private OrdinalSet ordinalSet;

    public OrdinalSet getOrdinalSet() {
        return ordinalSet;
    }

    public void setOrdinalSet(OrdinalSet ordinalSet) {
        this.ordinalSet = ordinalSet;
    }

    @Override
    public int compare(Job<?, ?> a, Job<?, ?> b) {
        if (ordinalSet != null) {
            return ordinalSet.expandWithOrdinalNumber(a.getFullName()).compareToIgnoreCase(
                    ordinalSet.expandWithOrdinalNumber(b.getFullName())
            );
        }
        return a.getFullName().compareToIgnoreCase(b.getFullName());
    }

    public static class OrdinalSet {
        private final List<String> entries;

        public OrdinalSet(List<String> entries) {
            this.entries = entries;
        }

        public List<String> getEntries() {
            return entries;
        }

        String expandWithOrdinalNumber(String name) {
            if (entries != null) {
                for(int i = 0; i< entries.size(); i++) {
                    name = name.replaceAll(entries.get(i), "#" + i + "_" + entries.get(i));
                }
            }
            return name;
        }

        public static OrdinalSet fromParameter(String ordinalSetParam) {
            List<String> entries = Arrays.stream(ordinalSetParam.split("<"))
                    .map(String::trim)
                    .collect(Collectors.toList());
            return new OrdinalSet(entries);
        }

        public String toParameter() {
            return String.join(" < ", entries);
        }
    }
}
