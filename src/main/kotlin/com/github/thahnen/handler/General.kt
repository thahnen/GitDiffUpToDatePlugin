package com.github.thahnen.handler

import java.io.File
import java.nio.file.Paths

import org.gradle.api.Project

import com.github.thahnen.util.BundleHandler
import com.github.thahnen.util.Git


/**
 *  General (Kenobi):
 *  ================
 *
 *  class to provide general functions for task handlers
 *
 *  @author thahnen
 */
internal class General {

    companion object {
        /**
         *  Returns the relative path of a file to the Gradle project root directory
         *
         *  @param target the Gradle project
         *  @param file the file to get the relative path of
         *  @return the relative path
         */
        internal fun getRelativePathToProject(target: Project, file: File) : String  {
            return Paths.get(target.projectDir.absolutePath).relativize(Paths.get(file.absolutePath)).toString()
        }


        /**
         *  Evaluates a UP-TO-DATE condition for a set of input / output files or folders
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param inputFiles necessary input dependencies for evaluation
         *  @param outputFiles necessary output files or folders for evaluation
         *  @return true when conditions fulfilled for input dependencies and output files or folders, false otherwise
         */
        internal fun evaluateConditionForInputOutput(target: Project, inputFiles : Set<File>,
                                                     outputFiles: Set<File>) : Boolean {
            // return false if:
            // - at least one dependency (file or folder) is not tracked using Git
            // - at least one dependency (file or folder) has difference to last commit
            // - at least one output file or folder is not tracked using Git
            when {
                !inputFiles.all {
                    Git.tracked(target.projectDir, getRelativePathToProject(target, it))
                } || !inputFiles.all {
                    Git.diff(target.projectDir, getRelativePathToProject(target, it))
                } || !outputFiles.all {
                    Git.tracked(target.projectDir, getRelativePathToProject(target, it))
                } -> return false
            }

            inputFiles.forEach {
                // return false if:
                // - at least one output file or folder is older than dependency
                val inputDependencyHash = Git.commit(target.projectDir, getRelativePathToProject(target, it))!!

                outputFiles.forEach { fileOrFolder ->
                    with (Git.commit(target.projectDir, getRelativePathToProject(target, fileOrFolder))!!) {
                        when {
                            Git.isParentCommit(target.projectDir, this, inputDependencyHash) -> return false
                        }
                    }
                }
            }

            // return true if:
            // - all dependencies and output files or folders are tracked AND
            // - output files or folders have a newer / the same hash as all the dependencies (maybe even diff)
            return true
        }


        /**
         *  Evaluates a UP-TO-DATE condition for a bundled task with given output file
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param bundleFile file object of bundled task output
         *  @param files necessary file dependencies for bundled task
         *  @param evaluateManifest if Manifest of output bundle should be evaluated when found from last build
         *  @return true when conditions fulfilled for bundle task to be UP-TO-DATE, false otherwise
         *
         *  TODO: Extend function with useFileOrFolderHashes!
         *  TODO: Extend function with failOnNotTracked!
         *
         *  TODO: Überarbeiten
         */
        internal fun evaluateConditionForBundledTask(target: Project, bundleFile : File, files : Set<File>,
                                                     evaluateManifest: Boolean) : Boolean {
            // return false if:
            // - JAR/WAR/EAR file doesn't exist
            // - at least one dependency (file or folder) has differences to last commit
            when {
                !bundleFile.exists() || !files.all {
                    Git.diff(target.projectDir, getRelativePathToProject(target, it))
                } -> return false
            }

            with (getRelativePathToProject(target, bundleFile)) {
                // check if JAR/WAR/EAR is part of Git repository and therefore has a commit hash of last change
                // INFO: Ignore if there is a diff in JAR/WAR/EAR file (could be due to re-running Gradle JAR/WAR/EAR
                //       task)!
                if (Git.tracked(target.projectDir, this)) {
                    // return true if:
                    // - commit of last change on JAR/WAR/EAR file is the same / newer than commit of last change on
                    //   each dependency
                    // - dependency is not tracked and parameter is true
                    // return false if:
                    // - commit of last change on JAR/WAR/EAR is older than commit of last change on each dependency
                    //   (incl. diff)
                    // - dependency is not tracked and parameter is false
                    return evaluateHashFiles(
                        target, Git.commit(target.projectDir, this)!!, files /*, failOnNotTracked*/
                    )
                }

                // check if evaluateManifest is set and therefore evaluation of Manifest attribute should be done
                if (evaluateManifest) {
                    val bundleManifestHash = BundleHandler(bundleFile).getCommitHash()
                    bundleManifestHash ?: return false

                    // return true if:
                    // - commit in JAR/WAR/EAR manifest is the same / newer than commit of last change on each dependency
                    // - dependency is not tracked and parameter is true
                    // return false if:
                    // - commit in JAR/WAR/EAR manifest is older than commit of last change on each dependency (incl.
                    //   diff)
                    // - dependency is not tracked and parameter is false
                    return evaluateHashFiles(target, bundleManifestHash, files /*, failOnNotTracked*/)
                }

                // JAR/WAR/EAR file exists (even tho not checked if newer than dependencies, just assumed) and
                // dependencies (files or folder) have no open differences to last commit
                return true
            }
        }


        /**
         *  Evaluates a hash against a set of files
         *
         *  @param target Gradle projects
         *  @param hash to check against files
         *  @param files to check against hash
         *  @param failOnNotTracked if evaluation should fail when file
         *  @return true when provided hash is equal or newer than hash of file
         */
        private fun evaluateHashFiles(target: Project, hash: String, files: Set<File>,
                                      failOnNotTracked: Boolean = false) : Boolean = files.all {
            val fileRelativePath = getRelativePathToProject(target, it)

            // check if file is tracked by Git
            when (Git.tracked(target.projectDir, fileRelativePath)) {
                false   -> !failOnNotTracked
                true    -> run {

                    // check if provided hash is equal or newer than hash of file
                    with (Git.commit(target.projectDir, fileRelativePath)!!) {
                        this == hash || Git.isParentCommit(target.projectDir, this, hash)
                    }
                }
            }
        }
    }
}
