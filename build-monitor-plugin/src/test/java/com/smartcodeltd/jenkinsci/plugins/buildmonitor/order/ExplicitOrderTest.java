package com.smartcodeltd.jenkinsci.plugins.buildmonitor.order;

import org.acegisecurity.AccessDeniedException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import static org.junit.Assert.assertTrue;

public class ExplicitOrderTest {
    @Test
    public void separatesBySpaceOrComma() {
        ExplicitOrder explicitOrder = new ExplicitOrder();
        explicitOrder.setExplicitOrder("alpha,beta, gamma delta");
        assertTrue(explicitOrder.compare(mockJob("alpha"), mockJob("beta")) < 0);
        assertTrue(explicitOrder.compare(mockJob("alpha"), mockJob("gamma")) < 0);
        assertTrue(explicitOrder.compare(mockJob("alpha"), mockJob("delta")) < 0);
        assertTrue(explicitOrder.compare(mockJob("beta"), mockJob("gamma")) < 0);
        assertTrue(explicitOrder.compare(mockJob("beta"), mockJob("delta")) < 0);
        assertTrue(explicitOrder.compare(mockJob("gamma"), mockJob("delta")) < 0);
    }

    @Test
    public void unlistedBuildsLast() {
        ExplicitOrder explicitOrder = new ExplicitOrder();
        explicitOrder.setExplicitOrder("alpha beta gamma delta");
        assertTrue(explicitOrder.compare(mockJob("alpha"), mockJob("omega")) < 0);
        assertTrue(explicitOrder.compare(mockJob("delta"), mockJob("omega")) < 0);
    }

    private class EmptyItemGroup implements ItemGroup<Item> {

        @Override
        public String getFullName() {
            return "";
        }

        @Override
        public String getFullDisplayName() {
            return "";
        }

        @Override
        public Collection<Item> getItems() {
            return Collections.emptyList();
        }

        @Override
        public String getUrl() {
            return "";
        }

        @Override
        public String getUrlChildPrefix() {
            return "";
        }

        @Override
        public Item getItem(String name) throws AccessDeniedException {
            return null;
        }

        @Override
        public File getRootDirFor(Item child) {
            return null;
        }

        @Override
        public void onRenamed(Item item, String oldName, String newName) throws IOException {

        }

        @Override
        public void onDeleted(Item item) throws IOException {

        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public File getRootDir() {
            return null;
        }

        @Override
        public void save() throws IOException {

        }
    }

    private class MockProject extends AbstractProject<MockProject, MockBuild> {

        protected MockProject(String name) {
            super(new EmptyItemGroup(), name);
        }

        @Override
        public DescribableList<Publisher, Descriptor<Publisher>> getPublishersList() {
            return null;
        }

        @Override
        protected Class<MockBuild> getBuildClass() {
            return null;
        }

        @Override
        public boolean isFingerprintConfigured() {
            return false;
        }


    }

    private class MockBuild extends AbstractBuild<MockProject, MockBuild> {

        protected MockBuild(MockProject job) throws IOException {
            super(job);
        }

        @Override
        public void run() {

        }

    }

    private Job<?, ?> mockJob(String fullName) {
        Job<?, ?> mock = new MockProject(fullName);
        return mock;
    }
}