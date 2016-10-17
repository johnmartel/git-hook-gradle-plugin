package ca.coglinc.gradle.plugins.githook

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

class GitHookPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        addCopyGitHooksTaskToProject(project)
    }

    private void addCopyGitHooksTaskToProject(Project project) {
        project.configure(project) {
            tasks.create( name:'copyGitHooks', type:Copy ) {
                outputs.upToDateWhen { false }

                String source = 'config/githooks'
                String destination = '.git/hooks'

                from source
                into destination

                doFirst {
                    logger.info("Copying git hooks from ${source} to ${destination}")
                }
            }
        }
    }
}
