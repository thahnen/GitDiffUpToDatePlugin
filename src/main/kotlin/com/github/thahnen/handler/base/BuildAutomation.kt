package com.github.thahnen.handler.base

import java.io.File

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar


/**
 *  BuildAutomation:
 *  ===============
 *
 *  interface for all build automation tools which can be used inside Gradle
 *
 *  @author thahnen
 */
internal interface BuildAutomation {

    /** method invoked when input files and Gradle bundled artifact task provided */
    fun <T: Jar> inputArtifactTask(target: Project, task: Task, propertyName: String, input: Set<File>,
                                   artifactTask: T, evaluateManifest: Boolean = false) : Boolean

    /** method invoked when input files and Gradle bundled artifact task provided, extra parameter */
    fun <T: Jar> inputArtifactTask(target: Project, task: Task, propertyName: String, input: Set<File>,
                                   artifactTask: T, evaluateManifest: Boolean, useFileHashes: Boolean = false) : Boolean
}
