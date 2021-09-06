package com.github.thahnen

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

import org.junit.BeforeClass
import org.junit.Test

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder

import com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties
import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable


/**
 *  GitDiffUpToDatePluginTest:
 *  =========================
 *
 *  jUnit test cases on the GitDiffUpToDatePlugin
 */
open class GitDiffUpToDatePluginTest {

    companion object {
        // test class location in Git repository ($buildDir/classes/kotlin/test)
        private val location = File(this::class.java.protectionDomain.codeSource.location.toURI())

        // test cases properties file
        private val correct1ProjectPropertiesPath   = this::class.java.classLoader.getResource("project_correct1.properties")!!
                                                        .path.replace("%20", " ")
        private val correct2ProjectPropertiesPath   = this::class.java.classLoader.getResource("project_correct2.properties")!!
                                                        .path.replace("%20", " ")
        private val wrong1ProjectPropertiesPath     = this::class.java.classLoader.getResource("project_wrong1.properties")!!
                                                        .path.replace("%20", " ")
        private val wrong2ProjectPropertiesPath     = this::class.java.classLoader.getResource("project_wrong2.properties")!!
                                                        .path.replace("%20", " ")
        private val wrong3ProjectPropertiesPath     = this::class.java.classLoader.getResource("project_wrong3.properties")!!
                                                        .path.replace("%20", " ")


        // test cases properties
        private val correct1Properties = Properties()
        private val correct2Properties = Properties()
        private val wrong1Properties = Properties()
        private val wrong2Properties = Properties()
        private val wrong3Properties = Properties()


        /** 0) Configuration to read properties once before running multiple tests using them */
        @Throws(IOException::class)
        @BeforeClass @JvmStatic fun configureTestsuite() {
            correct1Properties.load(FileInputStream(correct1ProjectPropertiesPath))
            correct2Properties.load(FileInputStream(correct2ProjectPropertiesPath))
            wrong1Properties.load(FileInputStream(wrong1ProjectPropertiesPath))
            wrong2Properties.load(FileInputStream(wrong2ProjectPropertiesPath))
            wrong3Properties.load(FileInputStream(wrong3ProjectPropertiesPath))

            println("location -> $location")
        }
    }


    /** 1) Tests only applying the plugin (without Git repository) */
    @Test fun testApplyPluginWithoutGitRepositoryToProject() {
        val project = ProjectBuilder.builder().build()

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work
            // INFO: equal to check on InvalidUserDataException as it is based on it
            assert(e.cause is NoGitRepositoryException)
        }

        assertEquals(false, project.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
    }


    /** 2) Tests only applying the plugin (without Java plugin applied) */
    @Test fun testApplyPluginWithoutJavaPluginToProject() {
        val project = ProjectBuilder.builder().withProjectDir(location).build()

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


    /** 3) Tests only applying the plugin (without environment variable / project properties used for configuration) */
    @Test fun testApplyPluginWithoutPropertiesToProject() {
        val project = ProjectBuilder.builder().withProjectDir(location).build()

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


    /** 4) Tests only applying the plugin (with project properties file, property empty) */
    @Test fun testApplyPluginWithPropertiesFileEmptyToProject() {
        listOf(wrong1Properties, wrong2Properties, wrong3Properties).forEach {
            val project = ProjectBuilder.builder().withProjectDir(location).build()

            // apply Java plugin
            project.pluginManager.apply(JavaPlugin::class.java)

            // project gradle.properties reference (can not be used directly!)
            val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
            it.keys.forEach { key ->
                propertiesExtension[key as String] = it.getProperty(key)
            }

            try {
                // try applying plugin (should fail)
                project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)
            } catch (e: Exception) {
                // assert applying did not work
                // INFO: equal to check on InvalidUserDataException as it is based on it
                assert(e.cause is PropertiesEntryInvalidException)
            }

            assertEquals(false, project.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
        }
    }


    /** 5) Tests only applying the plugin (correct properties file, task missing) */
    @Test fun testApplyPluginWithoutGradleTaskToProject() {
        val project = ProjectBuilder.builder().withProjectDir(location).build()

        // apply Java plugin
        project.pluginManager.apply(JavaPlugin::class.java)

        // project gradle.properties reference (can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        correct1Properties.keys.forEach { key ->
            propertiesExtension[key as String] = correct1Properties.getProperty(key)
        }

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work
            // INFO: equal to check on InvalidUserDataException as it is based on it
            assert(e.cause is TaskConfigurationTaskNameInvalidException)
        }

        assertEquals(false, project.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
    }


    /** 6) Tests only applying the plugin (correct properties file, file or folder missing) */
    @Test fun testApplyPluginWithoutFileOrFolderToProject() {
        val project = ProjectBuilder.builder().withProjectDir(location).build()

        // apply Java plugin
        project.pluginManager.apply(JavaPlugin::class.java)

        // project gradle.properties reference (can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        correct1Properties.keys.forEach { key ->
            propertiesExtension[key as String] = correct1Properties.getProperty(key)
        }

        // create new task as mentioned in properties file
        project.tasks.create("testTask")

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work
            // INFO: equal to check on InvalidUserDataException as it is based on it
            assert(e.cause is TaskConfigurationFileOrFolderInvalidException)
        }

        assertEquals(false, project.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
    }


    /** 7) Tests applying the plugin (with correct project properties file) */
    @Test fun testApplyPluginWithCorrectPropertiesToProject() {
        val project = ProjectBuilder.builder().withProjectDir(location).build()

        // apply Java plugin
        project.pluginManager.apply(JavaPlugin::class.java)

        // project gradle.properties reference (can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        correct2Properties.keys.forEach { key ->
            propertiesExtension[key as String] = correct2Properties.getProperty(key)
        }

        // create new task as mentioned in properties file
        project.tasks.create("testTask")

        // apply plugin
        project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)

        // assert that plugin was applied to the project
        assertTrue(project.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
    }


    /** 8) Tests applying the plugin (with correct project properties file in root project) */
    @Test fun testApplyPluginWithCorrectRootProjectPropertiesToProject() {
        val rootProject = ProjectBuilder.builder().build()
        val subProject = ProjectBuilder.builder().withParent(rootProject).withProjectDir(location).build()

        // apply Java plugin to subproject
        subProject.pluginManager.apply(JavaPlugin::class.java)

        // project gradle.properties reference (can not be used directly!)
        val propertiesExtension = rootProject.extensions.getByType(ExtraPropertiesExtension::class.java)
        correct2Properties.keys.forEach { key ->
            propertiesExtension[key as String] = correct2Properties.getProperty(key)
        }

        // create new task as mentioned in properties file
        subProject.tasks.create("testTask")

        // apply plugin to subproject
        subProject.pluginManager.apply(GitDiffUpToDatePlugin::class.java)

        // assert that plugin was applied to the subproject
        assertTrue(subProject.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
    }


    /** 9) Tests applying the plugin (with correct environment variables) */
    @Test fun testApplyPluginWithCorrectEnvironmentVariablesToProject() {
        val project = ProjectBuilder.builder().withProjectDir(location).build()

        // apply Java plugin
        project.pluginManager.apply(JavaPlugin::class.java)

        withEnvironmentVariable(
            GitDiffUpToDatePlugin.KEY_CONFIG, correct2Properties[GitDiffUpToDatePlugin.KEY_CONFIG] as String
        ).and(
            GitDiffUpToDatePlugin.KEY_MANIFEST, correct2Properties[GitDiffUpToDatePlugin.KEY_MANIFEST] as String
        ).and(
            GitDiffUpToDatePlugin.KEY_FOFHASHES, correct2Properties[GitDiffUpToDatePlugin.KEY_FOFHASHES] as String
        ).execute {
            // assert the environment variables are set correctly
            assertEquals(correct2Properties[GitDiffUpToDatePlugin.KEY_CONFIG] as String, System.getenv(GitDiffUpToDatePlugin.KEY_CONFIG))
            assertEquals(correct2Properties[GitDiffUpToDatePlugin.KEY_MANIFEST] as String, System.getenv(GitDiffUpToDatePlugin.KEY_MANIFEST))
            assertEquals(correct2Properties[GitDiffUpToDatePlugin.KEY_FOFHASHES] as String, System.getenv(GitDiffUpToDatePlugin.KEY_FOFHASHES))

            // create new task as mentioned in properties file
            project.tasks.create("testTask")

            // apply plugin
            project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)

            // assert that plugin was applied to the project
            assertTrue(project.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
        }
    }


    /** 10) Tests applying the plugin (with correct system properties) */
    @Test fun testApplyPluginWithCorrectSystemPropertiesToProject() {
        val project = ProjectBuilder.builder().withProjectDir(location).build()

        // apply Java plugin
        project.pluginManager.apply(JavaPlugin::class.java)

        restoreSystemProperties {
            System.setProperty(GitDiffUpToDatePlugin.KEY_CONFIG, correct2Properties[GitDiffUpToDatePlugin.KEY_CONFIG] as String)
            System.setProperty(GitDiffUpToDatePlugin.KEY_MANIFEST, correct2Properties[GitDiffUpToDatePlugin.KEY_MANIFEST] as String)
            System.setProperty(GitDiffUpToDatePlugin.KEY_FOFHASHES, correct2Properties[GitDiffUpToDatePlugin.KEY_FOFHASHES] as String)

            // assert that system properties are set correctly
            assertEquals(correct2Properties[GitDiffUpToDatePlugin.KEY_CONFIG] as String, System.getProperty(GitDiffUpToDatePlugin.KEY_CONFIG))
            assertEquals(correct2Properties[GitDiffUpToDatePlugin.KEY_MANIFEST] as String, System.getProperty(GitDiffUpToDatePlugin.KEY_MANIFEST))
            assertEquals(correct2Properties[GitDiffUpToDatePlugin.KEY_FOFHASHES] as String, System.getProperty(GitDiffUpToDatePlugin.KEY_FOFHASHES))

            // create new task as mentioned in properties file
            project.tasks.create("testTask")

            // apply plugin
            project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)

            // assert that plugin was applied to the project
            assertTrue(project.plugins.hasPlugin(GitDiffUpToDatePlugin::class.java))
        }
    }


    /** 11) Tests applying the plugin and evaluates that the extension set by plugin exists */
    @Test fun testEvaluatePluginExtension() {
        val project = ProjectBuilder.builder().withProjectDir(location).build()

        // apply Java plugin
        project.pluginManager.apply(JavaPlugin::class.java)

        // project gradle.properties reference (can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        correct2Properties.keys.forEach { key ->
            propertiesExtension[key as String] = correct2Properties.getProperty(key)
        }

        // create new task as mentioned in properties file
        project.tasks.create("testTask")

        // apply plugin
        project.pluginManager.apply(GitDiffUpToDatePlugin::class.java)

        // assert that extension exists and is configured correctly
        assertNotNull(project.extensions.findByName(GitDiffUpToDatePlugin.KEY_EXTENSION))

        val extension = project.extensions.getByType(GitDiffUpToDatePluginExtension::class.java)
        assertEquals((correct2Properties[GitDiffUpToDatePlugin.KEY_MANIFEST] as String).toBoolean(), extension.evaluateManifest.get())
        assertEquals((correct2Properties[GitDiffUpToDatePlugin.KEY_FOFHASHES] as String).toBoolean(), extension.useFileOrFolderHashes.get())

        assertEquals(
            GitDiffUpToDatePlugin.parseTaskConfigurations(correct2Properties[GitDiffUpToDatePlugin.KEY_CONFIG] as String),
            extension.tasks.get()
        )
    }
}
