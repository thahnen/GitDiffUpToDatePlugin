package com.github.thahnen

import org.junit.Test

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 *  GitDiffUpToDateObjectTest:
 *  =========================
 *
 *  jUnit test cases on the GitDiffUpToDateObject
 */
open class GitDiffUpToDateObjectTest {

    /** 1) Tests the GitDiffUpToDateObject for JaCoCo coverage */
    @Test fun testGitDiffUpToDateObject() {
        val obj = GitDiffUpToDateObject("testTask", setOf("testFolder", "testFile"), "artifactTask")

        assertEquals("testTask", obj.taskName)
        assertEquals(2, obj.filesOrFolders.size)
        assertTrue(obj.filesOrFolders.contains("testFolder"))
        assertTrue(obj.filesOrFolders.contains("testFile"))
        assertNotNull(obj.artifactTaskName)
        assertEquals("artifactTask", obj.artifactTaskName!!)
    }
}
