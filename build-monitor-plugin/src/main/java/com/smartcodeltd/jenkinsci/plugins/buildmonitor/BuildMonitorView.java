/*
 * The MIT License
 *
 * Copyright (c) 2013-2015, Jan Molak, SmartCode Ltd http://smartcodeltd.co.uk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.smartcodeltd.jenkinsci.plugins.buildmonitor;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.api.Respond;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.facade.StaticJenkinsAPIs;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.installation.BuildMonitorInstallation;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobViews;
import hudson.Extension;
import hudson.model.Descriptor.FormException;
import hudson.model.Job;
import hudson.model.ListView;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static hudson.Util.filter;

/**
 * @author Jan Molak
 */
public class BuildMonitorView extends ListView {
    @Extension
    public static final BuildMonitorDescriptor descriptor = new BuildMonitorDescriptor();

    private String title;

    /**
     * @param name  Name of the view to be displayed on the Views tab
     * @param title Title to be displayed on the Build Monitor; defaults to 'name' if not set
     */
    @DataBoundConstructor
    public BuildMonitorView(String name, String title) {
        super(name);

        this.title = title;
    }

    @SuppressWarnings("unused") // used in .jelly
    public String getTitle() {
        return isGiven(title) ? title : getDisplayName();
    }

    @SuppressWarnings("unused") // used in .jelly
    public boolean isEmpty() {
        return jobViews().isEmpty();
    }

    @SuppressWarnings("unused") // used in .jelly
    public String getCsrfCrumbFieldName() {
        return new StaticJenkinsAPIs().crumbFieldName();
    }

    @SuppressWarnings("unused") // used in the configure-entries.jelly form
    public String currentOrder() {
        return currentConfig().getOrder().getClass().getSimpleName();
    }
    
    @SuppressWarnings("unused") // used in the configure-entries.jelly form
    public String currentbuildFailureAnalyzerDisplayedField() {
        return currentConfig().getBuildFailureAnalyzerDisplayedField().getValue();
    }

    @SuppressWarnings("unused") // used in the configure-entries.jelly form
    public boolean isDisplayCommitters() {
        return currentConfig().shouldDisplayCommitters();
    }

    // used in the configure-entries.jelly and main-settings.jelly forms
    @SuppressWarnings("unused")
    public double getTextScale() {
        return currentConfig().getTextScale();
    }

    // used in the configure-entries.jelly and main-settings.jelly forms
    @SuppressWarnings("unused")
    public int getMaxColumns() {
        return currentConfig().getMaxColumns();
    }

    // used in the configure-entries.jelly and main-settings.jelly forms
    @SuppressWarnings("unused")
    public boolean isColourBlindMode() {
        return currentConfig().inColourBlindMode();
    }

    // used in the configure-entries.jelly and main-settings.jelly forms
    @SuppressWarnings("unused")
    public boolean isReduceMotion() {
        return currentConfig().reduceMotion();
    }

    // used in the configure-entries.jelly and main-settings.jelly forms
    @SuppressWarnings("unused")
    public boolean isShowBadges() {
        return currentConfig().showBadges();
    }

    private static final BuildMonitorInstallation installation = new BuildMonitorInstallation();

    @SuppressWarnings("unused") // used in index.jelly
    public BuildMonitorInstallation getInstallation() {
        return installation;
    }

    @SuppressWarnings("unused") // used in .jelly
    public boolean collectAnonymousUsageStatistics() {
        return descriptor.getPermissionToCollectAnonymousUsageStatistics();
    }
    
    @Override
    protected void initColumns() {
    }

    @Override
    protected void submit(StaplerRequest req) throws ServletException, IOException, FormException {
        super.submit(req);

        JSONObject json = req.getSubmittedForm();

        synchronized (this) {

            String requestedOrdering = req.getParameter("order");
            title                    = req.getParameter("title");
            String maxColumns        = req.getParameter("maxColumns");
            String textScale         = req.getParameter("textScale");

            currentConfig().setColourBlindMode(json.optBoolean("colourBlindMode", false));
            currentConfig().setReduceMotion(json.optBoolean("reduceMotion", false));
            currentConfig().setShowBadges(json.optBoolean("showBadges", false));
            currentConfig().setDisplayCommitters(json.optBoolean("displayCommitters", true));
            currentConfig().setBuildFailureAnalyzerDisplayedField(req.getParameter("buildFailureAnalyzerDisplayedField"));
            
            try {
                currentConfig().setOrder(orderIn(requestedOrdering));
            } catch (Exception e) {
                throw new FormException("Can't order projects by " + requestedOrdering, "order");
            }
            try {
                currentConfig().setMaxColumns(Integer.parseInt(maxColumns));
            } catch (Exception e) {
                throw new FormException("Invalid value of 'Maximum number of columns': '" + maxColumns + "' (should be double).", maxColumns);
            }
            try {
                currentConfig().setTextScale(Double.parseDouble(textScale));
            } catch (Exception e) {
                throw new FormException("Invalid value of 'Text scale': '" + textScale + "' (should be double).", textScale);
            }
        }
    }

    /**
     * Because of how org.kohsuke.stapler.HttpResponseRenderer is implemented
     * it can only work with net.sf.JSONObject in order to produce correct application/json output
     *
     * @return Json representation of JobViews
     * @throws Exception
     */
    @JavaScriptMethod
    public JSONObject fetchJobViews() throws Exception {
        return Respond.withSuccess(jobViews());
    }

    // --
    private boolean isGiven(String value) {
        return ! (value == null || "".equals(value));
    }

    private List<JobView> jobViews() {
        JobViews views = new JobViews(new StaticJenkinsAPIs(), currentConfig());

        //A little bit of evil to make the type system happy.
        @SuppressWarnings("unchecked")
        List<Job<?, ?>> projects = new ArrayList(filter(super.getItems(), Job.class));
        List<JobView> jobs = new ArrayList<JobView>();

        Collections.sort(projects, currentConfig().getOrder());

        for (Job project : projects) {
            jobs.add(views.viewOf(project));
        }

        return jobs;
    }

    /**
     * When Jenkins is started up, Jenkins::loadTasks is called.
     * At that point config.xml file is unmarshaled into a Jenkins object containing a list of Views, including BuildMonitorView objects.
     *
     * The unmarshaling process sets private fields on BuildMonitorView objects directly, ignoring their constructors.
     * This means that if there's a private field added to this class (say "config"), the previously persisted versions of this class can no longer
     * be correctly un-marshaled into the new version as they don't define the new field and the object ends up in an inconsistent state.
     *
     * @return the previously persisted version of the config object, default config, or the deprecated "order" object, converted to a "config" object.
     */
    private Config currentConfig() {
        if (creatingAFreshView()) {
            config = Config.defaultConfig();
        }
        else if (deserailisingFromAnOlderFormat()) {
            migrateFromOldToNewConfigFormat();
        }

        return config;
    }

    private boolean creatingAFreshView() {
        return config == null && order == null;
    }

    // Is config.xml saved in a format prior to version 1.6+build.150 of Build Monitor?
    private boolean deserailisingFromAnOlderFormat() {
        return config == null && order != null;
    }

    // If an older version of config.xml is loaded, "config" field is missing, but "order" is present
    private void migrateFromOldToNewConfigFormat() {
        Config c = new Config();
        c.setOrder(order);

        config = c;
        order = null;
    }

    @SuppressWarnings("unchecked")
    private Comparator<Job<?, ?>> orderIn(String requestedOrdering) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String packageName = this.getClass().getPackage().getName() + ".order.";

        return (Comparator<Job<?, ?>>) Class.forName(packageName + requestedOrdering).newInstance();
    }

    private Config config;

    @Deprecated // use Config instead
    private Comparator<Job<?, ?>> order;      // note: this field can be removed when people stop using versions prior to 1.6+build.150
}
