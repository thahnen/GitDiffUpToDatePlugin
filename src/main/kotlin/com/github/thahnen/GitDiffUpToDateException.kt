package com.github.thahnen

import org.gradle.api.InvalidUserDataException


/**
 *  Base extension for every extension thrown by this plugin
 *
 *  @author thahnen
 */
open class GitDiffUpToDateException(message: String) : InvalidUserDataException(message)


/**
 *  Exception thrown when Gradle project isn't located inside a Git repository (which is necessary)
 */
open class NoGitRepositoryException(message: String) : GitDiffUpToDateException(message)


/**
 *  Exception thrown when this plugin is applied to a project which does not have the Gradle provided Java plugin
 *  applied. This plugin tests against the project Jar tasks results (only provided by the Java plugin)!
 */
open class PluginAppliedUnnecessarilyException(message: String) : GitDiffUpToDateException(message)


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


/**
 *  Exception thrown when Jar file not found when trying to read in Manifest file
 */
open class JarFileNotFoundException(message: String) : GitDiffUpToDateException(message)


/**
 *  Exception thrown when accessing Jar file throws an IO exception or no Manifest file found inside archive
 */
open class JarFileIOException(message: String) : GitDiffUpToDateException(message)
