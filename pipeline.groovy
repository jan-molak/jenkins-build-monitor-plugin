// docs: https://jenkins.io/doc/pipeline/examples/
def version = 'unknown'

node('hi-speed && lxc-fedora25') {

    git url: 'git@github.com:jan-molak/jenkins-build-monitor-plugin.git', branch: 'master'

    with_browser_stack 'linux-x64', {
        withEnv(["JAVA_HOME=${tool 'jdk-1.8.latest'}", "M2_HOME=${tool 'maven-3.5.3'}", "PATH+EXTRA=${env.M2_HOME}/bin:${env.JAVA_HOME}/bin:${tool 'node-8.11.1'}/bin"]) {

            mvn "-version"

            stage 'Build'

            dir('build-monitor-plugin') {
                mvn "release-candidate:updateVersion"

                mvn "clean verify"
            }

            dir('build-monitor-acceptance') {
                mvn "clean verify"
            }

            version = read_property('version', 'build-monitor-plugin/target/classes/build-monitor.properties');

            assign_build_name version

            archive_junit_results 'build-monitor-plugin/target/surefire-reports/TEST-*.xml,build-monitor-plugin/target/javascript/test-results.xml'
            archive_artifacts     'build-monitor-plugin/target/*.hpi,pom.xml,build-monitor-plugin/pom.xml,build-monitor-acceptance/pom.xml,build-monitor-acceptance/build-monitor-acceptance-base/pom.xml,build-monitor-acceptance/build-monitor-acceptance-latest/pom.xml,build-monitor-acceptance/build-monitor-acceptance-base/target/failsafe-reports/*-output.txt,build-monitor-acceptance/build-monitor-acceptance-latest/target/failsafe-reports/*-output.txt'
            archive_junit_results 'build-monitor-acceptance/build-monitor-acceptance-base/target/failsafe-reports/TEST-*.xml,build-monitor-acceptance/build-monitor-accpetance-latest/target/failsafe-reports/TEST-*.xml'
            archive_html          'Serenity', 'build-monitor-acceptance/target/site/serenity'

            stage 'Publish to GitHub'

            create_and_push_release_branch_for version
        }
    }
}
// --

def assign_build_name (new_name) {
    currentBuild.setDisplayName(new_name)
}

def read_property(property_name, properties_file) {
    def matcher = readFile(properties_file) =~ "${property_name}=(.+)"

    if (! matcher) {
        throw "Couldn't find '${property_name}' in '${properties_file}" as Throwable
    }

    return matcher[0][1]
}

def archive_artifacts(artifacts) {
    step([$class: 'ArtifactArchiver', artifacts: artifacts])
}

def archive_junit_results(results) {
    step([$class: 'JUnitResultArchiver', testResults: results])
}

def archive_html(report_name, path) {
    publishHTML(target: [
            reportName : report_name,
            reportDir: path,
            reportFiles: 'index.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: false
    ])
}

def with_browser_stack(version, actions) {

    echo "> Using BrowserStackLocal for '${version}'"

    if (! env.BROWSERSTACK_AUTOMATION_KEY) {
        throw "Looks like you haven't specified the BROWSERSTACK_AUTOMATION_KEY env variable." as Throwable
    }

    def auth_key = env.BROWSERSTACK_AUTOMATION_KEY

    if (!fileExists("/var/tmp/BrowserStackLocal")) {
        exec "curl -k -v -sS https://www.browserstack.com/browserstack-local/BrowserStackLocal-${version}.zip > /var/tmp/BrowserStackLocal.zip", 'Downloading BrowserStackLocal'
        exec "unzip -o /var/tmp/BrowserStackLocal.zip -d /var/tmp", 'Unpacking BrowserStackLocal'
        exec "chmod +x /var/tmp/BrowserStackLocal", 'Making the BrowserStackLocal binary executable'
    }

    spawn "browserstack-${version}", "/var/tmp/BrowserStackLocal ${auth_key} -onlyAutomate -forcelocal"

    try {
        actions()
    } catch(e) {
        echo "ERROR: ${e}"
        throw e
    } finally {
        stop "browserstack-${version}"
    }
}

def spawn (name, executable) {
    exec "BUILD_ID=dontKillMe nohup ${executable} > /var/tmp/${name}.log 2>&1 & echo \$! > /var/tmp/${name}.pid", "Spawning ${name}"
    def pid = exec "cat /var/tmp/${name}.pid"

    echo "> Started ${name} with Process ID: ${pid}"
}

def stop (name) {
    def pid = exec "cat /var/tmp/${name}.pid"

    exec "kill ${pid}", "Stopping ${name} [${pid}]"
    exec "rm /var/tmp/${name}.pid", "Removing the PID file for ${name}"
}

def mvn (command) {
    // `sh` instead of `exec` as we actually do care about the output
    sh "mvn -B -e -q ${command}"
}

def create_and_push_release_branch_for(version) {
    sh "git checkout -b release-${version}"
    sh "git commit -a -m \"Release candidate v${version}\""
    sh "git push origin release-${version}"
}

def exec (command, description='') {
    if (description) {
        echo "> '${description}'"
    } else {
        echo "> '${command}'"
    }

    try {
        sh "#!/usr/bin/env bash\n ${command} | tail -n 1 > /tmp/pipeline"

        return readFile('/tmp/pipeline').split("\r?\n")[0]
    } catch(e) {
        echo "Executing '${description}': ${command} failed: ${e}"
        throw e
    }
}
