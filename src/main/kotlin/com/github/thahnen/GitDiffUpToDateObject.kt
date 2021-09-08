package com.github.thahnen


/**
 *  GitDiffUpToDateObject:
 *  =====================
 *
 *  Object representing a Gradle task and its corresponding files / folders
 *  The task name creating an artifact is optional!
 *
 *  @author thahnen
 */
data class GitDiffUpToDateObject(val taskName: String, val filesOrFolders: Set<String>, val artifactTaskName: String?)
