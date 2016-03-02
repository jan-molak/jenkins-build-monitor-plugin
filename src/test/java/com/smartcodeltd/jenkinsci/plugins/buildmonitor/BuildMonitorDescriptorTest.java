package com.smartcodeltd.jenkinsci.plugins.buildmonitor;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar.Loops.asFollows;
import static hudson.util.FormValidation.Kind.ERROR;
import static hudson.util.FormValidation.Kind.OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BuildMonitorDescriptorTest {

    private BuildMonitorDescriptor validator;

    @Rule public final JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        validator = new BuildMonitorDescriptor();
    }

    @Test
    public void form_validator_should_allow_valid_reg_ex_specifying_what_jobs_to_include() throws Exception {
        for (String regex : asFollows(null, "", ".*", "myproject-.*")) {
            assertThat(itShouldAllow(regex), validator.doCheckIncludeRegex(regex).kind, is(OK));
        }
    }

    @Test
    public void form_validator_should_advise_how_a_regex_could_be_improved() throws Exception {
        FormValidation result = validator.doCheckIncludeRegex(")");

        assertThat(result.kind, is(ERROR));
        assertThat(htmlDecoded(result.getMessage()), containsString("Unmatched closing ')'"));
    }

    @Test
    public void form_validator_should_allow_fontsize_in_good_range() throws Exception {
        FormValidation result = validator.doCheckFontSize("1.0");

        assertThat(result.kind, is(OK));
    }

    @Test
    public void form_validator_should_refuse_fontsize_under_0_3() throws Exception {
        FormValidation result = validator.doCheckFontSize("0");

        assertThat(result.kind, is(ERROR));
        assertThat(htmlDecoded(result.getMessage()), containsString("Must be >= 0.3 and <= 2"));
    }

    @Test
    public void form_validator_should_refuse_fontsize_over_2() throws Exception {
        FormValidation result = validator.doCheckFontSize("2.1");

        assertThat(result.kind, is(ERROR));
        assertThat(htmlDecoded(result.getMessage()), containsString("Must be >= 0.3 and <= 2"));
    }

    @Test
    public void form_validator_should_refuse_fontsize_not_float() throws Exception {
        FormValidation result = validator.doCheckFontSize("a");

        assertThat(result.kind, is(ERROR));
        assertThat(htmlDecoded(result.getMessage()), containsString("Must be float"));
    }

    @Test
    public void form_validator_should_allow_numberofcolumns_in_good_range() throws Exception {
        FormValidation result = validator.doCheckNumberOfColumns("1");

        assertThat(result.kind, is(OK));
    }

    @Test
    public void form_validator_should_refuse_numberofcolumns_under_1() throws Exception {
        FormValidation result = validator.doCheckNumberOfColumns("0");

        assertThat(result.kind, is(ERROR));
        assertThat(htmlDecoded(result.getMessage()), containsString("Must be >= 1 and <= 8"));
    }

    @Test
    public void form_validator_should_refuse_numberofcolumns_over_8() throws Exception {
        FormValidation result = validator.doCheckNumberOfColumns("10");

        assertThat(result.kind, is(ERROR));
        assertThat(htmlDecoded(result.getMessage()), containsString("Must be >= 1 and <= 8"));
    }

    @Test
    public void form_validator_should_refuse_numberofcolumns_not_integer() throws Exception {
        FormValidation result = validator.doCheckNumberOfColumns("a");

        assertThat(result.kind, is(ERROR));
        assertThat(htmlDecoded(result.getMessage()), containsString("Must be integer"));
    }

    private String htmlDecoded(String message) {
        return StringEscapeUtils.unescapeHtml(message);
    }

    private String itShouldAllow(String regex) {
        return "should allow " + regex;
    }
}