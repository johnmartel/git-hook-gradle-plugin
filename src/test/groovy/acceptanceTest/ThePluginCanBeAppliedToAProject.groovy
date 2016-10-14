package acceptanceTest

import org.apache.commons.io.filefilter.NameFileFilter
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ThePluginCanBeAppliedToAProject extends Specification {

    private static final String CONFIG_GIT_HOOKS_FOLDER_NAME = 'config/githooks'

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


    def """given the git-hook-gradle-plugin is applied to a project
        when I run the copyGitHooks task
        the provided hooks are copied to ./git/hooks folder"""() {

        given:

        File targetGitHooksFolder = projectDir.newFolder('.git', 'hooks')
        File sourceGitHooksFolder = projectDir.newFolder('config', 'githooks')
        projectDir.newFile("${CONFIG_GIT_HOOKS_FOLDER_NAME}/commit-msg")

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
        FilenameFilter filter = new NameFileFilter('commit-msg')
        String[] files = targetGitHooksFolder.list(filter)
        files?.length == 1
    }
}
