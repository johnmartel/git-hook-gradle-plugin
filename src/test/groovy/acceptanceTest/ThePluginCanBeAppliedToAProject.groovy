package acceptanceTest

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ThePluginCanBeAppliedToAProject extends Specification {

    @Rule
    TemporaryFolder projectDir = new TemporaryFolder()

    def """given the git-hook-gradle-plugin is applied to a project
        when I build the project
        then build is successful"""() {

        given:
        File buildFile = projectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'ca.coglinc.githook'
            }

            repositories {
                jcenter()
            }
        """

        when:
        BuildResult buildResult = GradleRunner.create()
            .withProjectDir(projectDir.getRoot())
            .withArguments('copyGitHooks')
            .withPluginClasspath()
            .build();

        then:
        buildResult.task(':copyGitHooks').outcome == TaskOutcome.SUCCESS
    }
}
