package com.github.thahnen


/**
 *  GitDiffUpToDateObject:
 *  =====================
 *
 *  Object representing a Gradle task and its corresponding files / folders
 *
 *  @author thahnen
 */
data class GitDiffUpToDateObject(val taskName: String, val filesOrFolders: Set<String>)
