package net.serenitybdd.integration.jenkins.client;

import com.google.common.base.Joiner;
import net.serenitybdd.integration.jenkins.logging.ErrorWitness;
import net.serenitybdd.integration.jenkins.logging.InfoWitness;
import net.serenitybdd.integration.jenkins.logging.LoggerOutputStream;
import net.serenitybdd.integration.jenkins.process.JenkinsProcess;
import org.jdeferred.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;

import static java.lang.String.format;
import static net.serenitybdd.integration.jenkins.process.JenkinsProcess.JENKINS_IS_FULLY_UP_AND_RUNNING;

public class JenkinsClient {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsClient.class);
    private static final int Max_Wait_Time = 5 * 60 * 1000;
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String OS_VERSION = System.getProperty("os.version").toLowerCase();

    private final JenkinsProcess process;
    private final JenkinsClientExecutor executor;

    public JenkinsClient(JenkinsClientExecutor executor, JenkinsProcess process) {
        this.executor = executor;
        this.process = process;
    }

    // user accounts
    // https://gist.github.com/hayderimran7/50cb1244cc1e856873a4
    // http://stackoverflow.com/questions/17716242/creating-user-in-jenkins-via-api

    public void registerAccount(String username, String password) {
        logger.info("Enabling Jenkins Security and registering account for '{}', identified by '{}'", username, password);

        Promise<Matcher, ?, ?> promise = process.promiseWhen("defining beans \\[authenticationManager\\]");

        executeGroovy(
                "def instance         = jenkins.model.Jenkins.getInstance()",
                "def usersCanRegister = true",
                "def realm            = new hudson.security.HudsonPrivateSecurityRealm(usersCanRegister)",
                format("realm.createAccount(\"%s\",\"%s\")", username, password),
                "instance.setSecurityRealm(realm)",
                "instance.save()"
        );

        try {
            promise.waitSafely(Max_Wait_Time);
            logger.info("Account for '{}' created", username);
        } catch (InterruptedException e) {
            throw new RuntimeException("Couldn't enable Jenkins Security", e);
        }
    }

    public void populateUpdateCenterCaches() {
        logger.info("FETCHING UPDATE CENTER");

        Promise<Matcher, ?, ?> promise = process.promiseWhen("Obtained the latest update center data file for UpdateSource default");

        executeGroovy(
            "def ucUrl = new URL('http://updates.jenkins-ci.org/update-center.json')",
            "def json  = hudson.model.DownloadService.loadJSON(ucUrl)",
            "def site  = jenkins.model.Jenkins.instance.updateCenter.getById('default')",
            "site.updateData(json, false)"
        );

        try {
            promise.waitSafely(Max_Wait_Time);
            logger.info("UPDATE CENTER RELOADED");
        } catch (InterruptedException e) {
            throw new RuntimeException("Couldn't update the Update Center caches.", e);
        }
    }

    public void installPlugin(String pluginName) {
        // http://stackoverflow.com/questions/7709993/how-can-i-update-jenkins-plugins-from-the-terminal

        executeCommand("install-plugin", pluginName, "-restart");
        process.waitUntil(JENKINS_IS_FULLY_UP_AND_RUNNING);
    }


    public void installPlugins(List<String> plugins) {
        for (String pluginName : plugins) {
            executeCommand("install-plugin", pluginName);
        }

        restart();
    }

    private void restart() {
        //Both Windows and WSL needs hard restart
        if (OS.contains("win") || OS_VERSION.contains("microsoft")) {
            hardRestart();
        } else {
            safeRestart();
        }
    }

    private void hardRestart() {
        try {
            safeShutdown();
            process.start();
            //Note: Do NOT wait here for Jenkins startup as that is handled within process.start()
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void safeShutdown() {
        process.getJenkinsLogWatcher().close();
        executeCommand("safe-shutdown");
        process.stop();
    }

    private void safeRestart() {
        executeCommand("safe-restart");

        process.waitUntil(JENKINS_IS_FULLY_UP_AND_RUNNING);
    }

    public void setExternalBuildResult(String projectName, String result) {
        executeCommand("set-external-build-result",
                "--job", projectName,
                "--result", result,
                "--log", format("%s finished with %s", projectName, result)
        );
    }

    private int executeGroovy(String... groovyScriptLines) {
        String script = Joiner.on(";\n").join(groovyScriptLines);

        return executor.call("groovy", "=").execute(withInput(script), info(logger), error(logger));
    }

    private int executeCommand(String... args) {
        return executor.call(args).execute(noManualInput(), info(logger), error(logger));
    }

    private InputStream noManualInput() {
        return withInput("");
    }

    private InputStream withInput(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    private OutputStream info(Logger logger) {
        return new LoggerOutputStream(new InfoWitness(logger));
    }

    private OutputStream error(Logger logger) {
        return new LoggerOutputStream(new ErrorWitness(logger));
    }
}
