package com.github.thahnen

import java.io.File
import java.nio.file.Paths

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import org.gradle.plugins.ear.Ear
import org.gradle.plugins.ear.EarPlugin

import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withGroovyBuilder


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
 *  @author thahnen
 */
open class GitDiffUpToDatePlugin : Plugin<Project> {

    companion object {
        // identifiers of properties connected to this plugin
        internal val KEY_CONFIG     = "plugins.gitdiffuptodate.config"
        internal val KEY_MANIFEST   = "plugins.gitdiffuptodate.evaluateManifest"
        internal val KEY_FOFHASHES  = "plugins.gitdiffuptodate.useFileOrFolderHashes"

        // extension name
        internal val KEY_EXTENSION = "GitDiffUpToDateExtension"

        /**
         *  Parses the configuration property used by this plugin to set of GitDiffUpToDateObjects
         *
         *  @param config necessary properties entry (not empty / blank)
         *  @return set of task configurations (task -> Set<file / folder path> + optional artifact task
         *  @throws PropertyContentInvalidException when a config does not contain at least task name / files or folders
         */
        @Throws(PropertyContentInvalidException::class)
        internal fun parseTaskConfigurations(config: String) : Set<GitDiffUpToDateObject> {
            val taskConfigurations: MutableSet<GitDiffUpToDateObject> = mutableSetOf()

            config.replace(" ", "").split(";").forEach {
                val list = it.split(":")
                if (list.size < 2 || list.size > 3) {
                    throw PropertyContentInvalidException("Task configuration provided: '$it' is invalid!")
                }

                val taskName = list[0]
                val filesOrFolders = list[1]

                taskConfigurations.add(GitDiffUpToDateObject(
                    taskName,
                    filesOrFolders.split(",").map { fileOrFolder -> fileOrFolder.trim() }.toSet(),
                    when (list.size) {
                        3       -> list[2]
                        else    -> null
                    }
                ))
            }

            return taskConfigurations
        }
    }


    /** Overrides the abstract "apply" function */
    override fun apply(target: Project) {
        // 1) check if project directory is inside Git repository (otherwise Git commands can't execute)
        if (!Git.status(target.projectDir)) {
            throw NoGitRepositoryException(
                "Plugin must be applied to a Gradle project located inside a Git repository to run the commands!"
            )
        }


        // 2) check if Java plugin applied to target (necessary because check on Jar task)
        if (!target.plugins.hasPlugin(JavaPlugin::class.java)) {
            throw PluginAppliedUnnecessarilyException("Plugin shouldn't be applied when Java plugin isn't used!")
        }


        // 3) check if WAR / EAR plugin applied to target (not necessary but can be configured)
        val warPluginApplied = target.plugins.hasPlugin(WarPlugin::class.java)
        val earPluginApplied = target.plugins.hasPlugin(EarPlugin::class.java)


        // 4) retrieve necessary properties entries
        val config = getPropertiesEntry(target, KEY_CONFIG)
        if (config.isEmpty() || config.isBlank()) {
            throw PropertiesEntryInvalidException("$KEY_CONFIG provided but invalid (empty / blank)!")
        }

        var evaluateManifest = true
        try {
            val evaluateManifestString = getPropertiesEntry(target, KEY_MANIFEST)
            if (evaluateManifestString.isEmpty() || evaluateManifestString.isBlank()) {
                throw PropertiesEntryInvalidException("$KEY_MANIFEST provided but invalid (empty / blank)!")
            }
            evaluateManifest = evaluateManifestString.toBoolean()
        } catch (ignored: MissingPropertiesEntryException) { }

        var useFileOrFolderHashes = false
        try {
            val useFileOrFolderHashesString = getPropertiesEntry(target, KEY_FOFHASHES)
            if (useFileOrFolderHashesString.isEmpty() || useFileOrFolderHashesString.isBlank()) {
                throw PropertiesEntryInvalidException("$KEY_FOFHASHES provided but invalid (empty / blank)!")
            }
            useFileOrFolderHashes = useFileOrFolderHashesString.toBoolean()
        } catch (ignored: MissingPropertiesEntryException) { }


        // 5) parse configuration string to actual task configuration & evaluate correctness
        val taskConfigurations = parseTaskConfigurations(config)
        taskConfigurations.forEach {
            target.tasks.findByName(it.taskName) ?: run {
                throw TaskConfigurationTaskNameInvalidException(
                    "Task ${it.taskName} could not be found for configuration in project ${target.name}!"
                )
            }

            it.filesOrFolders.forEach { fileOrFolder ->
                if (!target.file("${target.projectDir}/$fileOrFolder").exists()) {
                    throw TaskConfigurationFileOrFolderInvalidException(
                        "File / folder $fileOrFolder  connected to task ${it.taskName} does not exist relative to " +
                        "project directory!"
                    )
                }
            }

            it.artifactTaskName?.let { name ->
                target.tasks.findByName(name) ?: run {
                    throw TaskConfigurationArtifactTaskNameInvalidException(
                        "Artifact task $name could not be found for configuration in project ${target.name}!"
                    )
                }
            }
        }


        // 6) custom extension to store the data
        val extension = target.extensions.create<GitDiffUpToDatePluginExtension>(KEY_EXTENSION)
        extension.tasks.set(taskConfigurations)
        extension.evaluateManifest.set(evaluateManifest)
        extension.useFileOrFolderHashes.set(useFileOrFolderHashes)


        // 7) configure each task based on its files / folders
        taskConfigurations.forEach { task ->
            target.tasks.getByName(task.taskName) {
                when {
                    this is Jar                     -> handleBundledTask(
                        target, this, task.filesOrFolders, evaluateManifest, useFileOrFolderHashes
                    )
                    warPluginApplied && this is War -> handleBundledTask(
                        target, this, task.filesOrFolders, evaluateManifest, useFileOrFolderHashes
                    )
                    earPluginApplied && this is Ear -> handleBundledTask(
                        target, this, task.filesOrFolders, evaluateManifest, useFileOrFolderHashes
                    )
                    else                            -> {
                        task.artifactTaskName?.let {
                            val normalTask = this

                            target.tasks.getByName(it) {
                                when {
                                    this is Jar                     -> handleBundledArtifactTask(
                                        target, normalTask, this, task.filesOrFolders, evaluateManifest,
                                        useFileOrFolderHashes
                                    )
                                    warPluginApplied && this is War -> handleBundledArtifactTask(
                                        target, normalTask, this, task.filesOrFolders, evaluateManifest,
                                        useFileOrFolderHashes
                                    )
                                    earPluginApplied && this is Ear -> handleBundledArtifactTask(
                                        target, normalTask, this, task.filesOrFolders, evaluateManifest,
                                        useFileOrFolderHashes
                                    )
                                    else                            -> {
                                        // Artifact task provided but not of type Jar / War / Ear so only check if files
                                        // or folders haven't changed & output files exists!
                                        // -> Can cause problems, so be warned!
                                        target.logger.warn(
                                            "[${this@GitDiffUpToDatePlugin::class.simpleName}] Provided an artifact " +
                                            "task but is not of type Jar (or when corresponding plugin applied of " +
                                            "type War or Ear) which can lead to incomprehensible compile or even " +
                                            "runtime exceptions / errors! With this message you've been warned and " +
                                            "my job here is done!"
                                        )

                                        outputs.upToDateWhen {
                                            outputs.files.all { output ->
                                                output.exists()
                                            } && task.filesOrFolders.all {
                                                fileOrFolder -> Git.diff(target.projectDir, fileOrFolder)
                                            }
                                        }
                                    }
                                }
                            }
                        } ?: run {
                            // No artifact task provided so only check if files or folders haven't changed!
                            // -> Can cause problems, so be warned!
                            target.logger.warn(
                                "[${this@GitDiffUpToDatePlugin::class.simpleName}] Providing no artifact task name " +
                                "can lead to incomprehensible compile or even runtime exceptions / errors! With this " +
                                "message you've been warned and my job here is done!"
                            )

                            outputs.upToDateWhen {
                                task.filesOrFolders.all { fileOrFolder -> Git.diff(target.projectDir, fileOrFolder) }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     *  Tries to retrieve the necessary properties file entry (can be provided as system property / environment variable
     *  as well)
     *
     *  @param target the project which the plugin is applied to, may be sub-project
     *  @param propertyKey the key of the property which entry should be retrieved
     *  @return content of properties entry
     *  @throws MissingPropertiesEntryException when necessary properties' entry not found / provided
     */
    @Throws(MissingPropertiesEntryException::class)
    private fun getPropertiesEntry(target: Project, propertyKey: String) : String {
        return when {
            target.properties.containsKey(propertyKey)              -> target.properties[propertyKey]
            target.rootProject.properties.containsKey(propertyKey)  -> target.rootProject.properties[propertyKey]
            System.getProperties().containsKey(propertyKey)         -> System.getProperties()[propertyKey]
            System.getenv().containsKey(propertyKey)                -> System.getenv(propertyKey)
            else                                                    -> throw MissingPropertiesEntryException(
                                                                            "$propertyKey not provided to plugin!"
                                                                        )
        } as String
    }


    /**
     *  Handle Ant task but with a bundled artifact task with dependencies
     *
     *  @param target the project which the plugin is applied to, may be sub-project
     *  @param task normal task
     *  @param artifactTask bundle task in project
     *  @param filesOrFolders necessary dependencies for bundle task
     *  @param upToDateProperty
     *
     *  TODO: Extend with manifest attribute handling!
     */
    @Suppress("unused")
    private fun <T: Jar> handleAntTask(target: Project, task: Task, artifactTask: T, filesOrFolders: Set<String>,
                                       upToDateProperty: String) {
        task.ant.withGroovyBuilder {
            filesOrFolders.forEach {
                "uptodate"(
                    "property" to upToDateProperty,
                    "srcfile" to "${target.projectDir}/$it",
                    "targetfile" to "${artifactTask.destinationDirectory}/${artifactTask.archiveFileName}"
                )
            }
        }
    }


    /**
     *  Handle a bundled task with dependencies
     *
     *  @param target the project which the plugin is applied to, may be sub-project
     *  @param task bundle task in project
     *  @param filesOrFolders necessary dependencies for bundle task
     *  @param evaluateManifest if Manifest of output bundle should be evaluated when found from last build
     *  @param useFileOrFolderHashes if commit hashes of files or folders should be stored / evaluated in manifest file
     */
    private fun <T: Jar> handleBundledTask(target: Project, task: T, filesOrFolders: Set<String>,
                                           evaluateManifest: Boolean, useFileOrFolderHashes: Boolean) {
        // set JAR / WAR / EAR manifest attributes including current commit hash
        task.manifest.attributes[JarHandler.attributeCommitHash] = Git.hash(target.projectDir)
        if (useFileOrFolderHashes) {
            filesOrFolders.forEach {
                task.manifest.attributes[
                    JarHandler.attributeFileOrFolderHashTemplate.replace("FOFHASH", SHA256.hash(it))
                ] = Git.commit(target.projectDir, it)
            }
        }

        // set UP-TO-DATE status & remove dependsOn using evaluation function
        if (evaluateConditionForBundledTask(
                target, target.file("${task.destinationDirectory}/${task.archiveFileName}"),
                filesOrFolders, evaluateManifest //, useFileOrFolderHashes
            )) {
            task.dependsOn.clear()
            task.outputs.upToDateWhen { true }
        }
    }


    /**
     *  Handle normal task but with a bundled artifact task with dependencies
     *
     *  @param target the project which the plugin is applied to, may be sub-project
     *  @param task normal task
     *  @param artifactTask bundle task in project
     *  @param filesOrFolders necessary dependencies for bundle task
     *  @param evaluateManifest if Manifest of output bundle should be evaluated when found from last build
     *  @param useFileOrFolderHashes if commit hashes of files or folders should be stored / evaluated in manifest file
     */
    private fun <T: Jar> handleBundledArtifactTask(target: Project, task: Task, artifactTask: T,
                                                   filesOrFolders: Set<String>, evaluateManifest: Boolean,
                                                   useFileOrFolderHashes: Boolean) {
        // set artifact tasks JAR / WAR / EAR manifest attributes including current commit hash
        artifactTask.manifest.attributes[JarHandler.attributeCommitHash] = Git.hash(target.projectDir)
        if (useFileOrFolderHashes) {
            filesOrFolders.forEach {
                artifactTask.manifest.attributes[
                    JarHandler.attributeFileOrFolderHashTemplate.replace("FOFHASH", SHA256.hash(it))
                ] = Git.commit(target.projectDir, it)
            }
        }

        // set UP-TO-DATE status & remove dependsOn using evaluation function
        if (evaluateConditionForBundledTask(
                target, target.file("${artifactTask.destinationDirectory}/${artifactTask.archiveFileName}"),
                filesOrFolders, evaluateManifest //, useFileOrFolderHashes
            )) {
            task.dependsOn.clear()
            task.outputs.upToDateWhen { true }
        }
    }


    /**
     *  Evaluates a UP-TO-DATE condition for a bundled task with given output file
     *
     *  @param target the project which the plugin is applied to, may be sub-project
     *  @param bundleFile file object of bundled task output
     *  @param filesOrFolders necessary dependencies bundled task
     *  @param evaluateManifest if Manifest of output bundle should be evaluated when found from last build
     *  @return true when conditions fulfilled for bundle task to be UP-TO-DATE, false otherwise
     *
     *  TODO: Extend function with useFileOrFolderHashes!
     */
    private fun evaluateConditionForBundledTask(target: Project, bundleFile : File, filesOrFolders : Set<String>,
                                                evaluateManifest: Boolean) : Boolean {
        // return false if:
        // - JAR / WAR / EAR file doesn't exist
        // - at least one dependency (file or folder) has differences to last commit
        when {
            !bundleFile.exists()                                    -> return false
            !filesOrFolders.all { Git.diff(target.projectDir, it) } -> return false
        }

        // get path of JAR / WAR / EAR file relative to project dir
        val bundleRelativePath = Paths.get(target.projectDir.absolutePath).relativize(
            Paths.get(bundleFile.absolutePath)
        ).toString()

        // check if JAR / WAR / EAR is part of Git repository and therefore has a commit hash of last change
        if (Git.tracked(target.projectDir, bundleRelativePath)) {
            val bundleCommitHash = Git.commit(target.projectDir, bundleRelativePath)!!

            // return true if:
            // - commit of last changes on JAR / WAR / EAR file is the same or newer than commit of last changes on
            //   every dependency
            // -> return false otherwise
            // INFO: Ignore if there is a diff in JAR / WAR / EAR file (could be changes due to re-running Gradle JAR /
            //       WAR / EAR task)!
            return when {
                filesOrFolders.all {
                    val fileOrFolderHash = Git.commit(target.projectDir, it)!!
                    with (fileOrFolderHash) {
                        this == bundleCommitHash || Git.isParentCommit(
                            target.projectDir, this, bundleCommitHash
                        )
                    }
                }       -> true
                else    -> false
            }
        }

        // check if evaluateManifest is set and therefore evaluation of Manifest attribute should be done
        if (evaluateManifest) {
            val bundleManifestHash = JarHandler(bundleFile).getCommitHash()
            bundleManifestHash ?: return false

            // return true if:
            // - commit in JAR / WAR / EAR Manifest is the same or newer than commit of last changes on every dependency
            return when {
                filesOrFolders.all {
                    val fileOrFolderHash = Git.commit(target.projectDir, it)!!
                    with (fileOrFolderHash) {
                        this == bundleManifestHash || Git.isParentCommit(
                            target.projectDir, this, bundleManifestHash
                        )
                    }
                }       -> true
                else    -> false
            }
        }

        // JAR / WAR / EAR file exists (even tho not checked if newer than dependencies, just assumed) and dependencies
        // (files or folder) have no open differences to last commit
        return true
    }
}
