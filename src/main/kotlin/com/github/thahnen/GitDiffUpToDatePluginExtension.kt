package com.github.thahnen

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty


/**
 *  GitDiffUpToDatePluginExtension:
 *  ==============================
 *
 *  Extension to this plugin but not for configuration, only for storing data as project.ext / project.extra is not
 *  available when working with the configurations resolution strategy for dependencies
 *
 *  @author thahnen
 */
abstract class GitDiffUpToDatePluginExtension {

    /** stores all configured tasks of this project and its corresponding files / folder */
    abstract val tasks: SetProperty<GitDiffUpToDateObject>

    /** stores if Jar Manifest files should be evaluated */
    abstract val evaluateManifest : Property<Boolean>

    /** stores if Jar Manifest should contain Commit hashes of Gradle task dependencies */
    abstract val useFileOrFolderHashes : Property<Boolean>
}
