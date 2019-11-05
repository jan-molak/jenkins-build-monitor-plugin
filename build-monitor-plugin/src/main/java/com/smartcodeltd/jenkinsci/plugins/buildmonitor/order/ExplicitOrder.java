package com.smartcodeltd.jenkinsci.plugins.buildmonitor.order;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import hudson.model.Job;

public class ExplicitOrder implements Comparator<Job<?, ?>> {
    private Map<String, Integer> order;

    @Override
    public int compare(Job<?, ?> first, Job<?, ?> second) {
        int firstOrder = Integer.MAX_VALUE;
        int secondOrder = Integer.MAX_VALUE;
        if (order.containsKey(first.getFullName())) {
            firstOrder = order.get(first.getFullName());
        }
        if (order.containsKey(second.getFullName())) {
            secondOrder = order.get(second.getFullName());
        }
        return firstOrder - secondOrder;
    }

    public void setExplicitOrder(String explicitOrder) {
        order = new HashMap<String, Integer>();
        String[] builds = explicitOrder.split("[ ,]+");
        for (int i = 0; i < builds.length; i++) {
            order.put(builds[i], i);
        }
    }
}
