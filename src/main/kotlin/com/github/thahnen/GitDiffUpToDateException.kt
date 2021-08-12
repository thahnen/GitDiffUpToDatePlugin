package com.github.thahnen

import org.gradle.api.InvalidUserDataException


/**
 *  Base extension for every extension thrown by this plugin
 *
 *  @author thahnen
 */
open class GitDiffUpToDateException(message: String) : InvalidUserDataException(message)


/**
 *  Thrown when this plugin is applied to a project which does not have the Gradle provided Java plugin applied. This
 *  plugin tests against the project Jar tasks results (only provided by the Java plugin)!
 */
open class PluginAppliedUnnecessarilyException(message: String) : InvalidUserDataException(message)


/**
 *  Exception thrown when no configuration provided in either (root) projects gradle.properties file or system
 *  property / environment variable passed to Gradle call
 */
open class MissingPropertiesEntryException(message: String) : GitDiffUpToDateException(message)


/**
 *  Exception thrown when value of necessary properties entry is invalid (no content)
 */
open class PropertiesEntryInvalidException(message: String) : GitDiffUpToDateException(message)


/**
 *  Exception thrown when task name provided in necessary properties entry to be configured using this plugin could not
 *  be found in project tasks list
 */
open class TaskConfigurationTaskNameInvalidException(message: String) : GitDiffUpToDateException(message)


/**
 *  Exception thrown when file / folder provided connected to a task in configuration stored in necessary properties
 *  entry does not exist
 */
open class TaskConfigurationFileOrFolderInvalidException(message: String) : GitDiffUpToDateException(message)
