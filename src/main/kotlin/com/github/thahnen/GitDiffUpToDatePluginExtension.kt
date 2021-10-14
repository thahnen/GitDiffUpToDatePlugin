package com.github.thahnen

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty


/**
 *  GitDiffUpToDatePluginExtension:
 *  ==============================
 *
 *  Extension to this plugin but not for configuration, only for storing data as project.ext / project.extra is not
 *  available when working with the configurations' resolution strategy for dependencies!
 *
 *  @author thahnen
 */
abstract class GitDiffUpToDatePluginExtension {

    /** stores all simple Ant configurations */
    abstract val antConfigSimple: SetProperty<GitDiffUpToDateObject.AntInputOutputObject>

    /** stores all advanced Ant configurations */
    abstract val antConfigAdvanced: SetProperty<GitDiffUpToDateObject.AntInputArtifactObject>

    /** stores if for Ant configurations Jar Manifest files should be evaluated */
    abstract val antEvaluateManifest : Property<Boolean>

    /** stores if for Ant configurations Jar Manifest should contain Commit hashes of Gradle task dependencies */
    abstract val antUseFileOrFolderHashes : Property<Boolean>

    /** stores if for Ant configurations <srcresources> blocks should be used inside <uptodate> task */
    abstract val antUseSrcResourcesBlock : Property<Boolean>


    /** stores all simple Gradle configurations */
    abstract val gradleConfigSimple: SetProperty<GitDiffUpToDateObject.GradleInputOutputObject>

    /** stores all advanced Gradle configurations */
    abstract val gradleConfigAdvanced: SetProperty<GitDiffUpToDateObject.GradleInputArtifactObject>

    /** stores if for Gradle configurations Jar Manifest files should be evaluated */
    abstract val gradleEvaluateManifest : Property<Boolean>

    /** stores if for Gradle configurations Jar Manifest should contain Commit hashes of Gradle task dependencies */
    abstract val gradleUseFileOrFolderHashes : Property<Boolean>

    /** stores if for Gradle configurations all tasks prior to provided ones should be removed from dependsOn */
    abstract val gradleSkipTaskGraph : Property<Boolean>
}
