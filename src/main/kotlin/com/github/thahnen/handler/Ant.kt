package com.github.thahnen.handler

import java.io.File

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar

import org.gradle.kotlin.dsl.withGroovyBuilder

import com.github.thahnen.handler.base.BuildAutomation
import com.github.thahnen.util.BundleHandler
import com.github.thahnen.util.Git
import com.github.thahnen.util.SHA256


/**
 *  Ant:
 *  ===
 *
 *  class to handle Ant tasks using its own <uptodate> task
 *
 *  @author thahnen
 */
internal class Ant {

    companion object: BuildAutomation {
        /**
         *  Handle Ant task simply based on input and output files
         *
         *  @param target Gradle project
         *  @param task Ant task
         *  @param propertyName the name used in Ant <uptodate> task inside task provided
         *  @param input set of input files
         *  @param output set of output files
         *  @param usingSrcResourcesBlock if Ant statement should be constructed using <srcresources>, defaults to false
         */
        internal fun inputOutputTask(target: Project, task: Task, propertyName: String, input: Set<File>,
                                     output: Set<File>, usingSrcResourcesBlock: Boolean = false) {
            task.ant.withGroovyBuilder {
                if (output.size == 1 || !usingSrcResourcesBlock) {
                    // <uptodate property="{propertyName}" srcfile="{inputFile1}" targetfile="{targetFile1}" />
                    // <uptodate property="{propertyName}" srcfile="{inputFile1}" targetfile="{targetFile2}" />
                    //
                    // <uptodate property="{propertyName}" srcfile="{inputFile2}" targetfile="{targetFile1}" />
                    // <uptodate property="{propertyName}" srcfile="{inputFile2}" targetfile="{targetFile2}" />
                    //
                    // ...
                    input.forEach { inp ->
                        output.forEach { outp ->
                            "uptodate"(
                                "property" to propertyName,
                                "srcfile" to "${target.projectDir}/${General.getRelativePathToProject(target, inp)}",
                                "targetfile" to "${target.projectDir}/${General.getRelativePathToProject(target, outp)}"
                            )
                        }
                    }
                } else {
                    // Ant equivalent:
                    //
                    // <uptodate property="{propertyName}" targetfile="{targetFile1}">
                    //   <srcresources>
                    //     <fileset file="{inputFile1}" />
                    //     <fileset file="{inputFile2}" />
                    //     ...
                    //   </srcresources>
                    // </uptodate>
                    //
                    // <uptodate property="{propertyName}" targetfile="{targetFile2}">
                    //   <srcresources>
                    //     <fileset file="{inputFile1}" />
                    //     <fileset file="{inputFile2}" />
                    //     ...
                    //   </srcresources>
                    // </uptodate>
                    //
                    // ...
                    output.forEach { outp ->
                        "uptodate"(
                            "property" to propertyName,
                            "targetfile" to "${target.projectDir}/${General.getRelativePathToProject(target, outp)}"
                        ) {
                            "srcresources" {
                                input.forEach { inp ->
                                    "fileset"(
                                        "file" to "${target.projectDir}/${General.getRelativePathToProject(target, inp)}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        /**
         *  Handle Ant task based on input files and Gradle task producing an artifact
         *  -> internally calls inputArtifactTask(Project, Task, String, Set<File>, T, Boolean, Boolean?)
         *
         *  @param target Gradle project
         *  @param task Ant task
         *  @param propertyName the name used in Ant <uptodate> task inside task provided
         *  @param input set of input files
         *  @param artifactTask Gradle task
         *  @param evaluateManifest whether the manifest of artifact should be evaluated
         *  @return result of evaluation
         */
        override fun <T: Jar> inputArtifactTask(target: Project, task: Task, propertyName: String, input: Set<File>,
                                                artifactTask: T, evaluateManifest: Boolean) : Boolean {
            return inputArtifactTask(target, task, propertyName, input, artifactTask, evaluateManifest, false)
        }


        /**
         *  Handle Ant task based on input files and Gradle task producing an artifact
         *
         *  @param target Gradle project
         *  @param task Ant task
         *  @param propertyName the name used in Ant <uptodate> task inside task provided
         *  @param input set of input files
         *  @param artifactTask Gradle task
         *  @param evaluateManifest whether the manifest of artifact should be evaluated
         *  @param useFileHashes whether commit hashes of files should be evaluated in manifest file
         *  @return result of evaluation
         */
        override fun <T: Jar> inputArtifactTask(target: Project, task: Task, propertyName: String, input: Set<File>,
                                                artifactTask: T, evaluateManifest: Boolean, useFileHashes: Boolean) : Boolean {
            // set bundled artifact tasks manifest attributes including current commit hash when enabled
            if (evaluateManifest) {
                artifactTask.manifest.attributes[BundleHandler.attributeCommitHash] = Git.hash(target.projectDir)

                // set file hashes in bundled artifact tasks manifest attributes when enabled
                if (useFileHashes) {
                    input.forEach {
                        with (General.getRelativePathToProject(target, it)) {
                            artifactTask.manifest.attributes[
                                BundleHandler.attributeFileHashTemplate.replace(
                                    BundleHandler.attributeCommitHash.split(".")[1], SHA256.hash(this)
                                )
                            ] = Git.commit(target.projectDir, this)
                        }
                    }
                }
            }

            // Set Ant <uptodate> based on evaluated condition to true!
            // INFO: when condition is false, let Gradle / Ant decide what to do
            val evaluation = General.evaluateConditionForBundledTask(
                target, target.file("${artifactTask.destinationDirectory}/${artifactTask.archiveFileName}"),
                input, evaluateManifest /*, useFileHashes*/
            )

            when {
                evaluation -> task.ant.withGroovyBuilder {
                    "uptodate"("property" to propertyName, "value" to evaluation)
                }
            }

            return evaluation
        }
    }
}
