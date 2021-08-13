package com.github.thahnen

import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

import kotlin.test.assertEquals

import org.junit.Before
import org.junit.Test

import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder


/**
 *  GitDiffUpToDatePluginTest:
 *  =========================
 *
 *  jUnit test cases on the GitDiffUpToDatePlugin
 */
open class GitDiffUpToDatePluginTest {

    // test cases properties file
    private val correctProjectPropertiesPath    = this.javaClass.classLoader.getResource("project.properties")!!
                                                    .path.replace("%20", " ")
    private val wrongProjectPropertiesPath      = this.javaClass.classLoader.getResource("project_wrong.properties")!!
                                                    .path.replace("%20", " ")

    // test cases properties
    private val correctProperties = Properties()
    private val wrongProperties = Properties()


    /** 0) Configuration to read properties once before running multiple tests using them */
    @Throws(IOException::class)
    @Before fun configureTestsuite() {
        correctProperties.load(FileInputStream(correctProjectPropertiesPath))
        wrongProperties.load(FileInputStream(wrongProjectPropertiesPath))
    }


    /** 1) Tests only applying the plugin (without Java plugin applied) */
    @Test fun testApplyPluginWithoutJavaPluginToProject() {
        val project = ProjectBuilder.builder().build()

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work
            // INFO: equal to check on InvalidUserDataException as it is based on it
            assert(e.cause is PluginAppliedUnnecessarilyException)
        }

        assertEquals(false, project.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
    }


    /** 2) Tests only applying the plugin (without environment variable / project properties used for configuration) */
    @Test fun testApplyPluginWithoutPropertiesToProject() {
        val project = ProjectBuilder.builder().build()

        // apply Java plugin
        project.pluginManager.apply(JavaPlugin::class.java)

        assertEquals(true, project.plugins.hasPlugin(JavaPlugin::class.java))

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work
            // INFO: equal to check on InvalidUserDataException as it is based on it
            assert(e.cause is MissingPropertiesEntryException)
        }

        assertEquals(false, project.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
    }
}
