package com.github.thahnen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar

import org.gradle.kotlin.dsl.create

import com.github.thahnen.config.Evaluator
import com.github.thahnen.config.GradleProperties
import com.github.thahnen.config.Parser
import com.github.thahnen.handler.Ant
import com.github.thahnen.handler.Gradle
import com.github.thahnen.util.Git


/**
 *  GitDiffUpToDatePlugin:
 *  =====================
 *
 *  Plugin to configure Gradle tasks passed to this plugin to be UP-TO-DATE depending on Git diff results on files /
 *  folders also provided to this plugin!
 *
 *  Result: - target.extensions.getByType(GitDiffUpToDatePluginExtension::class.java) for the following properties
 *          - tasks                 -> set of GitDiffUpToDateObjects representing tasks and their files / folders +
 *                                     optional artifact task
 *          - evaluateManifest      -> if Manifest file of Jar files created by Jar tasks should be evaluated
 *          - useFileOrFolderHashes -> if commit hashes of dependencies should be stored / evaluated in Manifest file
 *
 *  TODO: Diese Doku überarbeiten
 *
 *  @author thahnen
 */
sealed class GitDiffUpToDatePlugin : Plugin<Project> {

    companion object {
        // extension name
        internal val KEY_EXTENSION = "GitDiffUpToDateExtension"


        /**
         *  Parses any nullable String to Boolean
         *
         *  @param input nullable string
         *  @return string converted to Boolean, defaults to false on null
         */
        internal fun parseBooleanString(input: String?) : Boolean = input?.let { input.toBoolean() } ?: false
    }


    /** Overrides the abstract "apply" function */
    override fun apply(target: Project) {
        // 1) check if project directory is inside Git repository (otherwise Git commands can't execute)
        if (!Git.status(target.projectDir)) {
            throw NoGitRepositoryException(
                "[${this::class.simpleName}.${this::apply.name}] Plugin must be applied to " +
                "a Gradle project located inside a Git repository to run the necessary commands to calculate any " +
                "UP-TO-DATE status!"
            )
        }

        // 2) check if Java plugin applied to target (necessary because check on Jar task)
        if (!target.plugins.hasPlugin(JavaPlugin::class.java)) {
            throw PluginAppliedUnnecessarilyException(
                "[${this::class.simpleName}.${this::apply.name}] Plugin should not be applied when Java plugin not " +
                "already applied. It is necessary for at least 75% of this plugins logic!"
            )
        }

        // 3) retrieve necessary Ant properties entries
        val antConfigSimple = GradleProperties.getLocalPropertiesEntry(target, GradleProperties.antConfigSimple)
        val antConfigAdvanced = GradleProperties.getLocalPropertiesEntry(target, GradleProperties.antConfigAdvanced)
        val antEvaluateManifest = parseBooleanString(
            GradleProperties.getGlobalPropertiesEntry(target, GradleProperties.antEvaluateManifest)
        )
        val antUseFileOrFolderHashes = parseBooleanString(
            GradleProperties.getGlobalPropertiesEntry(target, GradleProperties.antUseFileOrFolderHashes)
        )
        val antUseSrcResourcesBlock = parseBooleanString(
            GradleProperties.getGlobalPropertiesEntry(target, GradleProperties.antUseSrcResourcesBlock)
        )

        // 4) retrieve necessary Gradle properties entries
        val gradleConfigSimple = GradleProperties.getLocalPropertiesEntry(target, GradleProperties.gradleConfigSimple)
        val gradleConfigAdvanced = GradleProperties.getLocalPropertiesEntry(target, GradleProperties.gradleConfigAdvanced)
        val gradleEvaluateManifest = parseBooleanString(
            GradleProperties.getGlobalPropertiesEntry(target, GradleProperties.gradleEvaluateManifest)
        )
        val gradleUseFileOrFolderHashes = parseBooleanString(
            GradleProperties.getGlobalPropertiesEntry(target, GradleProperties.gradleUseFileOrFolderHashes)
        )
        val gradleSkipTaskGraph = parseBooleanString(
            GradleProperties.getGlobalPropertiesEntry(target, GradleProperties.gradleSkipTaskGraph)
        )

        // 5) check that at least on configuration provided
        multipleNull(antConfigSimple, antConfigAdvanced, gradleConfigSimple, gradleConfigAdvanced) {
            throw MissingPropertiesEntryException(
                "[${this::class.simpleName}.${this::apply.name}] No configuration provided for Ant / Gradle tasks " +
                "using properties '${GradleProperties.antConfigSimple}' / '${GradleProperties.antConfigAdvanced}' / " +
                "'${GradleProperties.gradleConfigSimple}' / '${GradleProperties.gradleConfigAdvanced}'! At least one " +
                "configuration must be provided otherwise applying this plugin is unnecessary!"
            )
        }

        // 6) parse configurations
        val antSimpleConfigurations = antConfigSimple?.let { Parser.parseAntSimpleConfig(target, it) }
        val antAdvancedConfigurations = antConfigAdvanced?.let { Parser.parseAntAdvancedConfig(target, it) }
        val gradleSimpleConfigurations = gradleConfigSimple?.let { Parser.parseGradleSimpleConfig(target, it) }
        val gradleAdvancedConfigurations = gradleConfigAdvanced?.let { Parser.parseGradleAdvancedConfig(target, it) }

        // 7) evaluate correctness of configurations
        antSimpleConfigurations?.let {
            it.forEach { config ->
                with (GradleProperties.antConfigSimple) {
                    Evaluator.evaluateInputTaskName(target, this, config)
                    Evaluator.evaluateFilesOrFolders(target, this, config, true)
                    Evaluator.evaluateFilesOrFolders(target, this, config, false)
                }
            }
        }

        antAdvancedConfigurations?.let {
            it.forEach { config ->
                with (GradleProperties.antConfigAdvanced) {
                    Evaluator.evaluateInputTaskName(target, this, config)
                    Evaluator.evaluateFilesOrFolders(target, this, config, true)
                    Evaluator.evaluateArtifactTask(target, this, config)
                }
            }
        }

        gradleSimpleConfigurations?.let {
            it.forEach { config ->
                with (GradleProperties.gradleConfigSimple) {
                    Evaluator.evaluateInputTaskName(target, this, config)
                    Evaluator.evaluateFilesOrFolders(target, this, config, true)
                    Evaluator.evaluateFilesOrFolders(target, this, config, false)
                }
            }
        }

        gradleAdvancedConfigurations?.let {
            it.forEach { config ->
                with (GradleProperties.gradleConfigAdvanced) {
                    Evaluator.evaluateInputTaskName(target, this, config)
                    Evaluator.evaluateFilesOrFolders(target, this, config, true)
                    Evaluator.evaluateArtifactTask(target, this, config)
                }
            }
        }

        // 8) custom extension to store the data
        val extension = target.extensions.create<GitDiffUpToDatePluginExtension>(KEY_EXTENSION)
        antSimpleConfigurations?.let { extension.antConfigSimple.set(it) }
        antAdvancedConfigurations?.let { extension.antConfigAdvanced.set(it) }
        gradleSimpleConfigurations?.let { extension.gradleConfigSimple.set(it) }
        gradleAdvancedConfigurations?.let { extension.gradleConfigAdvanced.set(it) }
        extension.antEvaluateManifest.set(antEvaluateManifest)
        extension.antUseFileOrFolderHashes.set(antUseFileOrFolderHashes)
        extension.antUseSrcResourcesBlock.set(antUseSrcResourcesBlock)
        extension.gradleEvaluateManifest.set(gradleEvaluateManifest)
        extension.gradleUseFileOrFolderHashes.set(gradleUseFileOrFolderHashes)
        extension.gradleSkipTaskGraph.set(gradleSkipTaskGraph)

        // 9) implement each configuration based on its content
        antSimpleConfigurations?.let {
            it.forEach { config ->
                Ant.inputOutputTask(
                    target,
                    target.tasks.getByName(config.taskName),
                    config.propertyName,
                    config.inputFilesOrFolders.flattenFilesOrFolders(target),
                    config.outputFilesOrFolders.flattenFilesOrFolders(target),
                    antUseSrcResourcesBlock
                )
            }
        }

        antAdvancedConfigurations?.let {
            it.forEach { config ->
                val evaluation = Ant.inputArtifactTask(
                    target,
                    target.tasks.getByName(config.taskName),
                    config.propertyName,
                    config.inputFilesOrFolders.flattenFilesOrFolders(target),
                    target.tasks.getByName(config.artifactTaskName) as Jar,
                    antEvaluateManifest,
                    antUseFileOrFolderHashes
                )

                when {
                    !evaluation -> target.logger.warn(
                        "[${this::class.simpleName}.${this::apply.name}] Evaluation for advanced Ant configuration " +
                        "'$config' concluded with not UP-TO-DATE!"
                    )
                }
            }
        }

        gradleSimpleConfigurations?.let {
            it.forEach { config ->
                val inputTask = target.tasks.getByName(config.taskName)
                val evaluation = Gradle.inputOutputTask(
                    target, inputTask, "Gradle_${config.taskName}_uptodate",
                    config.inputFilesOrFolders.flattenFilesOrFolders(target),
                    config.outputFilesOrFolders.flattenFilesOrFolders(target)
                )

                when {
                    evaluation && gradleSkipTaskGraph -> inputTask.dependsOn.clear()
                    !evaluation -> target.logger.warn(
                        "[${this::class.simpleName}.${this::apply.name}] Evaluation for simple Gradle configuration " +
                        "'$config' concluded with not UP-TO-DATE!"
                    )
                }
            }
        }

        gradleAdvancedConfigurations?.let {
            it.forEach { config ->
                val inputTask = target.tasks.getByName(config.taskName)
                val evaluation = Gradle.inputArtifactTask(
                    target, inputTask, "Gradle_${config.taskName}_uptodate",
                    config.inputFilesOrFolders.flattenFilesOrFolders(target),
                    target.tasks.getByName(config.artifactTaskName) as Jar,
                    gradleEvaluateManifest,
                    gradleUseFileOrFolderHashes
                )

                when {
                    evaluation && gradleSkipTaskGraph -> inputTask.dependsOn.clear()
                    !evaluation -> target.logger.warn(
                        "[${this::class.simpleName}.${this::apply.name}] Evaluation for advanced Gradle " +
                        "configuration '$config' concluded with not UP-TO-DATE!"
                    )
                }
            }
        }
    }
}
