/*
 * The MIT License
 *
 * Copyright (c) 2013, Jan Molak, SmartCode Ltd http://smartcodeltd.co.uk
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

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.order.ByName;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobViewConfiguration;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.BuildAugmentor;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.claim.Claim;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.ListView;
import hudson.model.ViewDescriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static hudson.Util.filter;

/**
 * @author Jan Molak
 */
public class BuildMonitorView extends ListView {

    private boolean showAvatars;

    private boolean showCulpritName;

	/**
     * @param name  Name of the view
     */
    @DataBoundConstructor
    public BuildMonitorView(String name) {
        super(name);
    }

    @Extension
    public static final class Descriptor extends ViewDescriptor {
        public Descriptor() {
            super(BuildMonitorView.class);
        }

        @Override
        public String getDisplayName() {
            return "Build Monitor View";
        }

        /**
         * Cut-n-paste from ListView$Descriptor as we cannot inherit from that class
         */
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
    }

    @Override
    protected void submit(StaplerRequest req) throws ServletException, IOException, FormException {
        super.submit(req);

        String requestedOrdering = req.getParameter("order");
        showAvatars = req.getParameter("showAvatars") != null;
        showCulpritName = req.getParameter("showCulpritName") != null;

        try {
            order = orderIn(requestedOrdering);
        } catch (Exception e) {
            throw new FormException("Can't order projects by " + requestedOrdering, "order");
        }
    }

    // defensive coding to avoid issues when Jenkins instantiates the plugin without populating its fields
    // https://github.com/jan-molak/jenkins-build-monitor-plugin/issues/43
    private Comparator<AbstractProject> currentOrderOrDefault() {
        return order == null ? new ByName() : order;
    }

    public String currentOrder() {
        return currentOrderOrDefault().getClass().getSimpleName();
    }

    private Comparator<AbstractProject> order = new ByName();

    @SuppressWarnings("unchecked")
    private Comparator<AbstractProject> orderIn(String requestedOrdering) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String packageName = this.getClass().getPackage().getName() + ".order.";

        return (Comparator<AbstractProject>) Class.forName(packageName + requestedOrdering).newInstance();
    }

    /**
     * Because of how org.kohsuke.stapler.HttpResponseRenderer is implemented
     * it can only work with net.sf.JSONObject in order to produce correct application/json
     * output
     *
     * @return
     * @throws Exception
     */
    @JavaScriptMethod
    public JSONObject fetchJobViews() throws Exception {
        return jsonFrom(jobViews());
    }

    public boolean isEmpty() {
        return jobViews().isEmpty();
    }


    private JSONObject jsonFrom(List<JobView> jobViews) throws IOException {
        ObjectMapper m = new ObjectMapper();

        return (JSONObject) JSONSerializer.toJSON("{jobs:" + m.writeValueAsString(jobViews) + "}");
    }

    private List<JobView> jobViews() {
        List<AbstractProject> projects = filter(super.getItems(), AbstractProject.class);
        List<JobView> jobs = new ArrayList<JobView>();

        Collections.sort(projects, currentOrderOrDefault());

        for (AbstractProject project : projects) {
            final JobViewConfiguration config = new JobViewConfiguration();
            config.setShowAvatars(showAvatars);
            config.setShowCulpritName(showCulpritName);
            jobs.add(JobView.of(project, withAugmentationsIfTheyArePresent(), config));
        }

        return jobs;
    }

    private BuildAugmentor withAugmentationsIfTheyArePresent() {
        return BuildAugmentor.fromDetectedPlugins();
    }

    public boolean getShowAvatars() {
		return showAvatars;
	}
}