package com.github.thahnen

import java.io.File
import java.nio.file.Paths

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.tasks.Jar

import org.gradle.kotlin.dsl.create


/**
 *  GitDiffUpToDatePlugin:
 *  =====================
 *
 *  Plugin to configure Gradle tasks passed to this plugin to be UP-TO-DATE depending on Git diff results on files /
 *  folders also provided to this plugin!
 *
 *  Result: - target.extensions.getByType(GitDiffUpToDatePluginExtension::class.java) for the following properties
 *          - tasks                 -> set of GitDiffUpToDateObjects representing tasks and their files / folders
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
         *  @return set of task configurations (task -> Set<file / folder path>
         */
        internal fun parseTaskConfigurations(config: String) : Set<GitDiffUpToDateObject> {
            val taskConfigurations: MutableSet<GitDiffUpToDateObject> = mutableSetOf()

            config.replace(" ", "").split(";").forEach {
                val (taskName: String, filesOrFolders: String) = it.split(":", limit = 2)

                taskConfigurations.add(GitDiffUpToDateObject(
                    taskName, filesOrFolders.split(",").map { fileOrFolder -> fileOrFolder.trim() }.toSet()
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


        // 3) retrieve necessary properties entries
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


        // 4) parse configuration string to actual task configuration & evaluate correctness
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
                        "File / folder $fileOrFolder  connected to task ${it.taskName} does not exist relative to project directory!"
                    )
                }
            }
        }


        // 5) custom extension to store the data
        val extension = target.extensions.create<GitDiffUpToDatePluginExtension>(KEY_EXTENSION)
        extension.tasks.set(taskConfigurations)
        extension.evaluateManifest.set(evaluateManifest)
        extension.useFileOrFolderHashes.set(useFileOrFolderHashes)


        // 6) configure each task based on its files / folders
        taskConfigurations.forEach { task ->
            target.tasks.getByName(task.taskName) {
                if (this is Jar) {
                    handleJarTask(target, this, task.filesOrFolders, evaluateManifest, useFileOrFolderHashes)
                } else {
                    outputs.upToDateWhen {
                        task.filesOrFolders.all { fileOrFolder -> Git.diff(target.projectDir, fileOrFolder) }
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
     *  Handle a Jar task with dependencies
     *
     *  @param target the project which the plugin is applied to, may be sub-project
     *  @param task Jar task in project
     *  @param filesOrFolders necessary dependencies for Jar task
     *  @param evaluateManifest if Manifest of output Jar should be evaluated when found from last build
     *  @param useFileOrFolderHashes
     */
    private fun handleJarTask(target: Project, task: Jar, filesOrFolders: Set<String>, evaluateManifest: Boolean,
                              useFileOrFolderHashes: Boolean) {
        // set Jar manifest attributes including current commit hash
        task.manifest.attributes[JarHandler.attributeCommitHash] = Git.hash(target.projectDir)
        if (useFileOrFolderHashes) {
            filesOrFolders.forEach {
                task.manifest.attributes[
                    JarHandler.attributeFileOrFolderHashTemplate.replace("FOFHASH", SHA256.hash(it))
                ] = Git.commit(target.projectDir, it)
            }
        }

        // set UP-TO-DATE status using evaluation function
        task.outputs.upToDateWhen {
            evaluateConditionForJarTask(
                target, target.file("${task.destinationDirectory}/${task.archiveFileName}"),
                filesOrFolders, evaluateManifest //, useFileOrFolderHashes
            )
        }
    }


    /**
     *  Evaluates a UP-TO-DATE condition for a Jar task with given output file (Jar)
     *
     *  @param target the project which the plugin is applied to, may be sub-project
     *  @param jarFile file object of Jar task output
     *  @param filesOrFolders necessary dependencies for Jar task
     *  @param evaluateManifest if Manifest of output Jar should be evaluated when found from last build
     *  @return true when conditions fulfilled for Jar task to be UP-TO-DATE, false otherwise
     *
     *  TODO: Extend function with useFileOrFolderHashes!
     */
    private fun evaluateConditionForJarTask(target: Project, jarFile : File, filesOrFolders : Set<String>,
                                            evaluateManifest: Boolean) : Boolean {
        // return false if:
        // - Jar file doesn't exist
        // - at least one dependency (file or folder) has differences to last commit
        when {
            !jarFile.exists()                                       -> return false
            !filesOrFolders.all { Git.diff(target.projectDir, it) } -> return false
        }

        // get path of Jar file relative to project dir
        val jarRelativePath = Paths.get(target.projectDir.absolutePath).relativize(Paths.get(jarFile.absolutePath)).toString()

        // check if Jar is part of Git repository and therefore has a commit hash of last change
        if (Git.tracked(target.projectDir, jarRelativePath)) {
            val jarCommitHash = Git.commit(target.projectDir, jarRelativePath)!!

            // return true if:
            // - commit of last changes on Jar file is the same or newer than commit of last changes on every dependency
            // -> return false otherwise
            // INFO: Ignore if there is a diff in Jar file (could be changes due to re-running Gradle Jar task)!
            return when {
                filesOrFolders.all {
                    val fileOrFolderHash = Git.commit(target.projectDir, it)!!
                    with (fileOrFolderHash) {
                        this == jarCommitHash || Git.isParentCommit(target.projectDir, this, jarCommitHash)
                    }
                }       -> true
                else    -> false
            }
        }

        // check if evaluateManifest is set and therefore evaluation of Manifest attribute should be done
        if (evaluateManifest) {
            val jarManifestHash = JarHandler(jarFile).getCommitHash()
            jarManifestHash ?: return false

            // return true if:
            // - commit in Jar Manifest is the same or newer than commit of last changes on every dependency
            return when {
                filesOrFolders.all {
                    val fileOrFolderHash = Git.commit(target.projectDir, it)!!
                    with (fileOrFolderHash) {
                        this == jarManifestHash || Git.isParentCommit(target.projectDir, this, jarManifestHash)
                    }
                }       -> true
                else    -> false
            }
        }

        // Jar file exists (even tho not checked if newer than dependencies, just assumed) and dependencies (files or
        // folder) have no open differences to last commit
        return true
    }
}
