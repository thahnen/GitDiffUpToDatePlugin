package com.github.thahnen

import kotlin.reflect.full.companionObject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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


    /** 2) Tests if function "parseTaskConfigurations" throws an exception */
    @Test fun testParseTaskConfigurationIncorrectData() {
        listOf("first", "first : second : third : fourth").forEach {
            var tryCatch = false

            try {
                // try parse string to configuration
                GitDiffUpToDatePlugin.parseTaskConfigurations(it)
            } catch (e: Exception) {
                // assert parsing did not work
                // INFO: equal to check on InvalidUserDataException as it is based on it
                assert(e is PropertyContentInvalidException)

                tryCatch = true
            }

            assertTrue(tryCatch)
        }
    }


    /** 3) Tests if function "parseTaskConfigurations" performs parsing of configuration correctly (no artifact task) */
    @Test fun testParseTaskConfigurationWithoutArtifact() {
        val inputString = "classes : Test123.java, Test456.java; test : /var/tests/resource.png, /var/tests/xml/"
        val expectedOutput = setOf(
            GitDiffUpToDateObject("classes", setOf("Test123.java", "Test456.java"), null),
            GitDiffUpToDateObject("test", setOf("/var/tests/resource.png", "/var/tests/xml/"), null)
        )

        val realOutput = GitDiffUpToDatePlugin.parseTaskConfigurations(inputString)
        assertEquals(expectedOutput.size, realOutput.size)

        realOutput.forEach { config ->
            assertNull(config.artifactTaskName)

            assertTrue(
                expectedOutput.any { it.taskName == config.taskName && it.filesOrFolders == config.filesOrFolders }
            )
        }
    }


    /** 4) Tests if function "parseTaskConfigurations" performs parsing of configuration correctly (with artifact task) */
    @Test fun testParseTaskConfigurationWithArtifact() {
        val inputString = "classes : Test123.java, Test456.java : jar; test : /var/tests/resource.png, /var/tests/xml/ : jar"
        val expectedOutput = setOf(
            GitDiffUpToDateObject("classes", setOf("Test123.java", "Test456.java"), "jar"),
            GitDiffUpToDateObject("test", setOf("/var/tests/resource.png", "/var/tests/xml/"), "jar")
        )

        val realOutput = GitDiffUpToDatePlugin.parseTaskConfigurations(inputString)
        assertEquals(expectedOutput.size, realOutput.size)

        realOutput.forEach { config ->
            assertNotNull(config.artifactTaskName)

            assertTrue(
                expectedOutput.any {
                    it.taskName == config.taskName && it.filesOrFolders == config.filesOrFolders
                    && it.artifactTaskName!! == config.artifactTaskName!!
                }
            )
        }
    }
}
