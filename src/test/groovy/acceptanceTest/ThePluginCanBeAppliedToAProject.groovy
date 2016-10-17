package acceptanceTest

import org.apache.commons.io.filefilter.NameFileFilter
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ThePluginCanBeAppliedToAProject extends Specification {

    private static final String GIT_FOLDER_NAME = '.git'
    private static final String HOOKS_FOLDER_NAME = 'hooks'
    private static final String CONFIG_FOLDER_NAME = 'config'
    private static final String GITHOOKS_FOLDER_NAME = 'githooks'
    private static final String COMMIT_MSG_FILE_NAME = "commit-msg"
    private static final String COPY_GIT_HOOKS_TASK_NAME = 'copyGitHooks'

    private File defaultBuildFile

    @Rule
    TemporaryFolder projectDir = new TemporaryFolder()


    def setup() {
        defaultBuildFile = projectDir.newFile('build.gradle')
        defaultBuildFile << """
            plugins {
                id 'ca.coglinc.githook'
            }

            repositories {
                jcenter()
            }
        """

    }

    def """given the git-hook-gradle-plugin is applied to a project
        when I build the project
        then build do not fail"""() {

        when:
        BuildResult buildResult = GradleRunner.create()
            .withProjectDir(projectDir.getRoot())
            .withArguments(COPY_GIT_HOOKS_TASK_NAME)
            .withPluginClasspath()
            .build();

        then:
        buildResult.task(':copyGitHooks').outcome != TaskOutcome.FAILED
    }

    def """given the git-hook-gradle-plugin is applied to a project
        when I run the copyGitHooks task
        the provided hooks are copied to ./git/hooks folder"""() {

        given:
        File targetGitHooksFolder = projectDir.newFolder(GIT_FOLDER_NAME, HOOKS_FOLDER_NAME)
        File sourceGitHooksFolder = projectDir.newFolder(CONFIG_FOLDER_NAME, GITHOOKS_FOLDER_NAME)

        new File(sourceGitHooksFolder, COMMIT_MSG_FILE_NAME).createNewFile()

        when:
        BuildResult buildResult = GradleRunner.create()
                .withProjectDir(projectDir.getRoot())
                .withArguments(COPY_GIT_HOOKS_TASK_NAME)
                .withPluginClasspath()
                .build();

        then:
        buildResult.task(':copyGitHooks').outcome == TaskOutcome.SUCCESS
        FilenameFilter filter = new NameFileFilter(COMMIT_MSG_FILE_NAME)
        String[] files = targetGitHooksFolder.list(filter)
        files?.length == 1
    }

    def """given the git-hook-gradle-plugin is applied to a project with a git hooks file
        when I run the copyGitHooks task
        the provided hooks override correctly ./git/hooks folder"""() {

        given:
        File targetGitHooksFolder = projectDir.newFolder(GIT_FOLDER_NAME, HOOKS_FOLDER_NAME)
        File sourceGitHooksFolder = projectDir.newFolder(CONFIG_FOLDER_NAME, GITHOOKS_FOLDER_NAME)

        File originCommitMsgHook = new File(targetGitHooksFolder, COMMIT_MSG_FILE_NAME)
        originCommitMsgHook.createNewFile()
        originCommitMsgHook.text = "test1"

        File newCommitMsgHook = new File(sourceGitHooksFolder, COMMIT_MSG_FILE_NAME)
        newCommitMsgHook.createNewFile()
        newCommitMsgHook.text = "test2"

        when:
        BuildResult buildResult = GradleRunner.create()
                .withProjectDir(projectDir.getRoot())
                .withArguments(COPY_GIT_HOOKS_TASK_NAME)
                .withPluginClasspath()
                .build();

        then:
        buildResult.task(':copyGitHooks').outcome == TaskOutcome.SUCCESS
        FilenameFilter filter = new NameFileFilter(COMMIT_MSG_FILE_NAME)
        String[] files = targetGitHooksFolder.list(filter)
        files?.length == 1
        new File(targetGitHooksFolder, files[0]).text == "test2"
    }
}
