package com.smartcodeltd.jenkinsci.plugin.assetbundler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.TestCase.assertTrue;

public class PathToAssetTest {

    @Test
    public void combines_base_resource_url_given_by_jenkins_and_a_path_to_the_asset() throws Exception {
        URL base = baseResourceUrlGivenByJenkins("file:///opt/jenkins/plugins/build-monitor-plugin/");

        PathToAsset path = new PathToAsset(base, "less/index.less");
        //Tweaking it to be able to run on Windows
        String actualValue = absolute(path).replace("\\", "/");
        String expectedValue = "/opt/jenkins/plugins/build-monitor-plugin/less/index.less";
        assertTrue("\nActual: " + actualValue + " \n does not contain expected value : " + expectedValue, actualValue.contains(expectedValue));
    }

    @Test
    public void works_with_unc_paths_on_windows() throws Exception {
        URL base = baseResourceUrlGivenByJenkins("file://server/jenkins/plugins/build-monitor-plugin/");

        PathToAsset path = new PathToAsset(base, "less/index.less");
        //Tweaking it to be able to run on Windows
        String actualValue = absolute(path).replace("\\", "/");
        String expectedValue = "/server/jenkins/plugins/build-monitor-plugin/less/index.less";
        assertTrue("\nActual: " + actualValue + " \n does not contain expected value : " + expectedValue, actualValue.contains(expectedValue));
    }

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void complains_when_given_a_non_file_base_resource_url() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Sorry, can't load the asset from 'http://google.com/less/index.less' using the 'file' protocol");

        new PathToAsset(new URL("http://google.com"), "less/index.less");
    }

    // --

    private String absolute(PathToAsset path) {
        return path.toFile().getAbsolutePath();
    }

    // a pass-through method, only here for readability purposes
    private URL baseResourceUrlGivenByJenkins(String url) throws MalformedURLException {
        return new URL(url);
    }
}