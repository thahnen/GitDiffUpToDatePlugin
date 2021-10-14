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
 *  Gradle:
 *  ======
 *
 *  class to handle Gradle tasks using its outputs.upToDateWhen { ... } specification
 *
 *  @author thahnen
 */
internal class Gradle {

    companion object: BuildAutomation {
        /**
         *  Handle Gradle task simply based on input and output files
         *
         *  @param target Gradle project
         *  @param task Ant task
         *  @param propertyName the name used in Ant property to indicate UP-TO-DATE status for (possible) following use
         *  @param input set of input files
         *  @param output set of output files
         */
        internal fun inputOutputTask(target: Project, task: Task, propertyName: String, input: Set<File>,
                                     output: Set<File>) : Boolean {
            val evaluation = General.evaluateConditionForInputOutput(target, input, output)
            when {
                evaluation -> task.outputs.upToDateWhen { true }
            }

            task.ant.withGroovyBuilder {
                "property"("name" to propertyName, "value" to evaluation)
            }

            return evaluation
        }


        /**
         *  Handle Gradle task based on input files and task producing an artifact
         *  -> internally calls inputArtifactTask(Project, Task, String, Set<File>, T, Boolean, Boolean?)
         *
         *  @param target Gradle project
         *  @param task Gradle task
         *  @param propertyName the name used in Ant property to indicate UP-TO-DATE status for (possible) following use
         *  @param input set of input files
         *  @param artifactTask Gradle task
         *  @param evaluateManifest whether the manifest of artifact should be evaluated, defaults to false
         *  @return result of evaluation
         */
        override fun <T: Jar> inputArtifactTask(target: Project, task: Task, propertyName: String, input: Set<File>,
                                                artifactTask: T, evaluateManifest: Boolean) : Boolean {
            return inputArtifactTask(target, task, propertyName, input, artifactTask, evaluateManifest, false)
        }


        /**
         *  Handle Gradle task based on input files and task producing an artifact
         *
         *  @param target Gradle project
         *  @param task Gradle task
         *  @param propertyName the name used in Ant property to indicate UP-TO-DATE status for (possible) following use
         *  @param input set of input files
         *  @param artifactTask Gradle task
         *  @param evaluateManifest whether the manifest of artifact should be evaluated
         *  @param useFileHashes whether commit hashes of files should be evaluated in manifest file, defaults to false
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

            // Set Gradle outputs.upToDateWhen { ... } specification based on evaluated condition to true!
            // INFO: when condition is false, let Gradle / Ant decide what to do
            val evaluation = General.evaluateConditionForBundledTask(
                target, target.file("${artifactTask.destinationDirectory}/${artifactTask.archiveFileName}"),
                input, evaluateManifest /*, useFileHashes*/
            )

            when {
                evaluation -> task.outputs.upToDateWhen { true }
            }

            task.ant.withGroovyBuilder {
                "property"("name" to propertyName, "value" to evaluation)
            }

            return evaluation
        }
    }
}
