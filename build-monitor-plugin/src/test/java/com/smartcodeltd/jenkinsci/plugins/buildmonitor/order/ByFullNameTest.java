package com.smartcodeltd.jenkinsci.plugins.buildmonitor.order;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.order.ByFullName.OrdinalSet;
import hudson.model.ItemGroup;
import hudson.model.Job;
import junit.framework.TestCase;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ByFullNameTest extends TestCase {

    public void testCompare_havingNoOrdinalSet() {
        // when
        ByFullName comparator = new ByFullName();
        ItemGroup parent = mock(ItemGroup.class);
        when(parent.getFullName()).thenReturn("Home");
        Job job_A = new WorkflowJob(parent, "Job A");
        Job job_a = new WorkflowJob(parent, "Job a");
        Job job_B = new WorkflowJob(parent, "Job B");
        // then
        assertThat(comparator.compare(job_A, job_a), is(0));
        assertThat(comparator.compare(job_A, job_B), lessThan(0));
        assertThat(comparator.compare(job_B, job_A), greaterThan(0));
    }

    public void testCompare_havingAnOrdinalSet() {
        // when
        ByFullName comparator = new ByFullName();
        comparator.setOrdinalSet(new OrdinalSet(Arrays.asList("CI_Pipeline", "Deploy_Test", "QA_Check", "Deploy_Prod")));
        ItemGroup folderA = mock(ItemGroup.class);
        when(folderA.getFullName()).thenReturn("AlphaApp");
        Job jobA1 = new WorkflowJob(folderA, "CI_Pipeline");
        Job jobA2 = new WorkflowJob(folderA, "Deploy_Test");
        Job jobA3 = new WorkflowJob(folderA, "QA_Check");
        Job jobA4 = new WorkflowJob(folderA, "Deploy_Prod");
        ItemGroup folderB = mock(ItemGroup.class);
        when(folderB.getFullName()).thenReturn("BetaApp");
        Job jobB1 = new WorkflowJob(folderB, "CI_Pipeline");
        Job jobB2 = new WorkflowJob(folderB, "Deploy_Test");
        Job jobB3 = new WorkflowJob(folderB, "QA_Check");
        Job jobB4 = new WorkflowJob(folderB, "Deploy_Prod");
        Job jobB5 = new WorkflowJob(folderB, "RegressionTest");
        Job jobB6 = new WorkflowJob(folderB, "SmokeTest");
        // then
        assertThat(comparator.compare(jobA1, jobA2), lessThan(0));
        assertThat(comparator.compare(jobA2, jobA3), lessThan(0));
        assertThat(comparator.compare(jobA3, jobA4), lessThan(0));
        assertThat(comparator.compare(jobA4, jobB1), lessThan(0));
        assertThat(comparator.compare(jobB1, jobB2), lessThan(0));
        assertThat(comparator.compare(jobB2, jobB3), lessThan(0));
        assertThat(comparator.compare(jobB3, jobB4), lessThan(0));
        assertThat(comparator.compare(jobB4, jobB5), lessThan(0));
        assertThat(comparator.compare(jobB5, jobB6), lessThan(0));
    }

    public void testGetFullNameWithOrdinalIndex() {
        // when
        OrdinalSet ordinalSet = new OrdinalSet(Arrays.asList("CI_Pipeline", "Deploy_Test", "Deploy_Prod"));
        // then
        assertThat(ordinalSet.expandWithOrdinalNumber("DemoApp/CI_Pipeline"), is("DemoApp/#0_CI_Pipeline"));
        assertThat(ordinalSet.expandWithOrdinalNumber("DemoApp/Deploy_Prod"), is("DemoApp/#2_Deploy_Prod"));
        assertThat(ordinalSet.expandWithOrdinalNumber("DemoApp/QA_Check"), is("DemoApp/QA_Check"));
    }

    public void testOrdinalSetFromParameter() {
        // when
        OrdinalSet ordinalSet = OrdinalSet.fromParameter("CI_Pipeline<Deploy_Test < Deploy_Prod");
        // then
        List<String> expectedEntries = Arrays.asList("CI_Pipeline", "Deploy_Test", "Deploy_Prod");
        assertEquals(expectedEntries, ordinalSet.getEntries());
    }

    public void testOrdinalSetToParameter() {
        // when
        OrdinalSet ordinalSet = new OrdinalSet(Arrays.asList("CI_Pipeline", "Deploy_Test", "Deploy_Prod"));
        // then
        assertThat(ordinalSet.toParameter(), is("CI_Pipeline < Deploy_Test < Deploy_Prod"));
    }
}
