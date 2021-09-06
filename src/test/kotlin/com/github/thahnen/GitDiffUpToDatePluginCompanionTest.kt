package com.github.thahnen

import kotlin.reflect.full.companionObject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import org.junit.Test

import com.github.thahnen.GitDiffUpToDatePlugin.Companion.KEY_CONFIG
import com.github.thahnen.GitDiffUpToDatePlugin.Companion.KEY_MANIFEST
import com.github.thahnen.GitDiffUpToDatePlugin.Companion.KEY_FOFHASHES
import com.github.thahnen.GitDiffUpToDatePlugin.Companion.KEY_EXTENSION


/**
 *  GitDiffUpToDatePluginCompanionTest:
 *  ==================================
 *
 *  jUnit test cases on the companion object of GitDiffUpToDatePlugin
 */
class GitDiffUpToDatePluginCompanionTest {

    /** 1) Tests if constants exist */
    @Test fun testConstantsExist() {
        assertTrue(
            GitDiffUpToDatePlugin::class.java.kotlin.companionObject!!.members.any { it.name == ::KEY_CONFIG.name }
        )
        assertTrue(
            GitDiffUpToDatePlugin::class.java.kotlin.companionObject!!.members.any { it.name == ::KEY_MANIFEST.name }
        )
        assertTrue(
            GitDiffUpToDatePlugin::class.java.kotlin.companionObject!!.members.any { it.name == ::KEY_FOFHASHES.name }
        )
        assertTrue(
            GitDiffUpToDatePlugin::class.java.kotlin.companionObject!!.members.any { it.name == ::KEY_EXTENSION.name }
        )
    }


    /** 2) Tests if function "parseTaskConfigurations" performs parsing of configuration correctly */
    @Test fun testParseTaskConfiguration() {
        val inputString = "classes : Test123.java, Test456.java; test : /var/tests/resource.png, /var/tests/xml/"
        val expectedOutput = setOf(
            GitDiffUpToDateObject("classes", setOf("Test123.java", "Test456.java")),
            GitDiffUpToDateObject("test", setOf("/var/tests/resource.png", "/var/tests/xml/"))
        )

        val realOutput = GitDiffUpToDatePlugin.parseTaskConfigurations(inputString)
        assertEquals(expectedOutput.size, realOutput.size)

        realOutput.forEach { config ->
            assertTrue(
                expectedOutput.any { it.taskName == config.taskName && it.filesOrFolders == config.filesOrFolders }
            )
        }
    }
}
