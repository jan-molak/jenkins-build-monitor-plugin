package net.serenitybdd.integration.jenkins.environment.rules;

import com.smartcodeltd.aether.ArtifactTransporter;
import net.serenitybdd.integration.jenkins.JenkinsArtifactTransporter;
import net.serenitybdd.integration.jenkins.JenkinsInstance;
import net.serenitybdd.integration.jenkins.client.JenkinsClient;
import net.serenitybdd.integration.jenkins.client.JenkinsClientExecutor;
import net.serenitybdd.integration.jenkins.process.JenkinsProcess;
import net.serenitybdd.integration.utils.CommandLineTools;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.IOException;
import java.nio.file.Path;

public class ManageJenkinsServer implements ApplicativeTestRule<JenkinsInstance> {
    private final Path javaExecutable;
    private final ArtifactTransporter transporter;

    public ManageJenkinsServer() {
        this(JenkinsArtifactTransporter.create());
    }

    public ManageJenkinsServer(ArtifactTransporter transporter) {
        this(CommandLineTools.java(), transporter);
    }

    public ManageJenkinsServer(Path javaExecutable, ArtifactTransporter artifactTransporter) {
        this.javaExecutable = javaExecutable;
        this.transporter = artifactTransporter;
    }

    @Override
    public TestRule applyTo(final JenkinsInstance jenkins) {
        return new TestWatcher() {
            private JenkinsProcess process;
            private JenkinsClient client;

            @Override
            protected void starting(Description description) {
                process = jenkinsProcessFor(jenkins, warFileOf(jenkins));

                try {
                    process.start();
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't start Jenkins", e);
                }

                JenkinsClientExecutor executor = new JenkinsClientExecutor(jenkins.url());
                client = new JenkinsClient(executor, process);
                jenkins.setClient(client);
            }

            @Override
            protected void finished(Description description) {
                client.safeShutdown();
            }
        };
    }

    private Path warFileOf(JenkinsInstance jenkins) {
        return transporter.get("org.jenkins-ci.main", "jenkins-war", jenkins.version(), "war");
    }

    private JenkinsProcess jenkinsProcessFor(JenkinsInstance jenkins, Path war) {
        Path java        = CommandLineTools.java();

        return new JenkinsProcess(java, war, jenkins.port(), jenkins.home());
    }
}
