package com.github.thahnen

import kotlin.reflect.full.companionObject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

import org.junit.Test

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
            GitDiffUpToDatePlugin::class.java.kotlin.companionObject!!.members.any { it.name == ::KEY_EXTENSION.name }
        )
    }


    /** 2) Tests function "parseBooleanString" */
    @Test fun testParseTaskConfigurationIncorrectData() {
        assertFalse(GitDiffUpToDatePlugin.parseBooleanString(null))
        assertFalse(GitDiffUpToDatePlugin.parseBooleanString("false"))
        assertTrue(GitDiffUpToDatePlugin.parseBooleanString("true"))
    }
}
