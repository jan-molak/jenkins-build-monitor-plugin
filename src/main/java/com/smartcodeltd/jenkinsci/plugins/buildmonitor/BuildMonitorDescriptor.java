package com.smartcodeltd.jenkinsci.plugins.buildmonitor;

import hudson.Util;
import hudson.model.ViewDescriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class BuildMonitorDescriptor extends ViewDescriptor {

    public BuildMonitorDescriptor() {
        super(BuildMonitorView.class);
        load();
    }

    @Override
    public String getDisplayName() {
        return "Build Monitor View";
    }

    // Copy-n-paste from ListView$Descriptor as sadly we cannot inherit from that class
    public FormValidation doCheckIncludeRegex(@QueryParameter String value) {
        String v = Util.fixEmpty(value);
        if (v != null) {
            try {
                Pattern.compile(v);
            } catch (PatternSyntaxException pse) {
                return FormValidation.error(pse.getMessage());
            }
        }
        return FormValidation.ok();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json.getJSONObject("build-monitor"));
        save();

        return true;
    }

    @SuppressWarnings("unused") // used in the configure-entries.jelly form
    public FormValidation doCheckFontSize(@QueryParameter String value) {
        try {
            float val = Float.parseFloat(value);
            if (val >= 0.3 && val <= 2) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Must be >= 0.3 and <= 2");
            }
        } catch (NumberFormatException e) {
            return FormValidation.error("Must be float");
        }
    }

    @SuppressWarnings("unused") // used in the configure-entries.jelly form
    public FormValidation doCheckNumberOfColumns(@QueryParameter String value) {
        try {
            int val = Integer.parseInt(value);
            if (val >= 1 && val <= 8) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Must be >= 1 and <= 8");
            }
        } catch (NumberFormatException e) {
            return FormValidation.error("Must be integer");
        }
    }

    private boolean permissionToCollectAnonymousUsageStatistics = true;

    public boolean getPermissionToCollectAnonymousUsageStatistics() {
        return this.permissionToCollectAnonymousUsageStatistics;
    }

    @SuppressWarnings("unused") // used in global.jelly
    public void setPermissionToCollectAnonymousUsageStatistics(boolean collect) {
        this.permissionToCollectAnonymousUsageStatistics = collect;
    }
}