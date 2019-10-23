package com.smartcodeltd.jenkinsci.plugin.assetbundler;

import org.apache.commons.lang.SystemUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PathToAssetTest {
    
    private static final String WINDOWS_SEPARATOR = "\\\\";
    private static final String UNIX_SEPARATOR = "/";

    @Test
    public void combines_base_resource_url_given_by_jenkins_and_a_path_to_the_asset() throws Exception {
        URL base = baseResourceUrlGivenByJenkins("file:///opt/jenkins/plugins/build-monitor-plugin/");

        PathToAsset path = new PathToAsset(base, "less/index.less");

        assertThat(normalize(absolute(path)), is("/opt/jenkins/plugins/build-monitor-plugin/less/index.less"));
    }

    @Test
    public void works_with_unc_paths_on_windows() throws Exception {
        URL base = baseResourceUrlGivenByJenkins("file://server/jenkins/plugins/build-monitor-plugin/");

        PathToAsset path = new PathToAsset(base, "less/index.less");

        assertThat(normalize(absolute(path)), is("/server/jenkins/plugins/build-monitor-plugin/less/index.less"));
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

    /**
     * Normalize the path to get a constant value between windows and *nix
     */
    private String normalize(String path) {
        String normalized = path;
        
        if (SystemUtils.IS_OS_WINDOWS) {
            normalized = normalized.replaceAll(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
            if (normalized.indexOf(":") >= 0) {
                normalized = normalized.substring(normalized.indexOf(":") + 1);
            }
            if (normalized.startsWith("//")) {
                normalized = normalized.substring(1);
            }
        }
        
        return normalized;
    }

    // a pass-through method, only here for readability purposes
    private URL baseResourceUrlGivenByJenkins(String url) throws MalformedURLException {
        return new URL(url);
    }
}