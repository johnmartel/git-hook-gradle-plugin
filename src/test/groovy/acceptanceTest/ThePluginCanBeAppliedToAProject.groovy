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
    private File targetGitHooksFolder
    private File sourceGitHooksFolder

    @Rule
    TemporaryFolder projectDir = new TemporaryFolder()

    def setup() {
        targetGitHooksFolder = projectDir.newFolder(GIT_FOLDER_NAME, HOOKS_FOLDER_NAME)
        sourceGitHooksFolder = projectDir.newFolder(CONFIG_FOLDER_NAME, GITHOOKS_FOLDER_NAME)
    }

    def """given the git-hook-gradle-plugin is applied to a project
        when I build the project
        then build do not fail"""() {
        given:
        givenABasicBuildFile()

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
        givenABasicBuildFile()
        givenAGitHookFileIsProvidedByTheProjectSource()

        when:
        BuildResult buildResult = GradleRunner.create()
            .withProjectDir(projectDir.getRoot())
            .withArguments(COPY_GIT_HOOKS_TASK_NAME)
            .withPluginClasspath()
            .build();

        then:
        buildResult.task(':copyGitHooks').outcome == TaskOutcome.SUCCESS
        String[] files = listFilesFromTargetGitHooksFolder()
        files?.length == 1
        new File(targetGitHooksFolder, files[0]).canExecute()
    }

    private String[] listFilesFromTargetGitHooksFolder() {
        FilenameFilter filter = new NameFileFilter(COMMIT_MSG_FILE_NAME)
        return targetGitHooksFolder.list(filter)
    }

    def """given the git-hook-gradle-plugin is applied to a project with a git hooks file
        when I run the copyGitHooks task
        the provided hooks override correctly ./git/hooks folder"""() {

        given:
        givenABasicBuildFile()
        givenAGitHookFileAlreadyExistsInProject()
        givenAGitHookFileIsProvidedByTheProjectSource()

        when:
        BuildResult buildResult = GradleRunner.create()
                .withProjectDir(projectDir.getRoot())
                .withArguments(COPY_GIT_HOOKS_TASK_NAME)
                .withPluginClasspath()
                .build();

        then:
        buildResult.task(':copyGitHooks').outcome == TaskOutcome.SUCCESS
        String[] files = listFilesFromTargetGitHooksFolder()
        files?.length == 1
        new File(targetGitHooksFolder, files[0]).canExecute()
        new File(targetGitHooksFolder, files[0]).text == "test2"
    }

    def """given the git-hook-gradle-plugin is applied to a java project
        when I run the processResources task
        the provided hooks are copied to ./git/hooks folder"""() {

        given:
        givenAJavaBuildFile()
        givenAGitHookFileIsProvidedByTheProjectSource()

        when:
        BuildResult buildResult = GradleRunner.create()
            .withProjectDir(projectDir.getRoot())
            .withArguments('processResources')
            .withPluginClasspath()
            .build();

        then:
        String[] files = listFilesFromTargetGitHooksFolder()
        files?.length == 1
        new File(targetGitHooksFolder, files[0]).canExecute()
    }


    private void givenAGitHookFileAlreadyExistsInProject() {
        File originCommitMsgHook = new File(targetGitHooksFolder, COMMIT_MSG_FILE_NAME)
        originCommitMsgHook.createNewFile()
        originCommitMsgHook.text = "test1"
    }

    private void givenAGitHookFileIsProvidedByTheProjectSource() {
        File newCommitMsgHook = new File(sourceGitHooksFolder, COMMIT_MSG_FILE_NAME)
        newCommitMsgHook.createNewFile()
        newCommitMsgHook.text = "test2"
    }

    private void givenABasicBuildFile() {
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

    private void givenAJavaBuildFile() {
        defaultBuildFile = projectDir.newFile('build.gradle')
        defaultBuildFile << """
            plugins {
                id 'java'
                id 'ca.coglinc.githook'
            }

            repositories {
                jcenter()
            }
        """
    }
}
