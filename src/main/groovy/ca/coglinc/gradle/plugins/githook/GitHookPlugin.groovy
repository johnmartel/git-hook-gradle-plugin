package ca.coglinc.gradle.plugins.githook

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitHookPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('copyGitHooks') << {
            project.logger.info 'no git hooks to be copied'
        }
    }
}
