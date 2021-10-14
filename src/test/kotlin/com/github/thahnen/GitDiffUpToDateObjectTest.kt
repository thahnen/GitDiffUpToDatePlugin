package com.github.thahnen

import org.junit.Test

import kotlin.test.assertEquals


/**
 *  GitDiffUpToDateObjectTest:
 *  =========================
 *
 *  jUnit test cases on the GitDiffUpToDateObject
 */
open class GitDiffUpToDateObjectTest {

    companion object {
        // test data used
        private val taskName = "testTask"
        private val propertyName = "testProperty"
        private val inputFiles = setOf("input1", "input2")
        private val outputFiles = setOf("output1", "output2")
        private val artifactTaskName = "testArtifactTask"
    }

    /** 1) Tests the GitDiffUpToDateObject.AntInputOutputObject for JaCoCo coverage */
    @Test fun test_GitDiffUpToDateObject_AntInputOutputObject() {
        val obj = GitDiffUpToDateObject.AntInputOutputObject(taskName, propertyName, inputFiles, outputFiles)

        assertEquals(taskName, obj.taskName)
        assertEquals(propertyName, obj.propertyName)
        assertEquals(inputFiles, obj.inputFilesOrFolders)
        assertEquals(outputFiles, obj.outputFilesOrFolders)
    }


    /** 2) Tests the GitDiffUpToDateObject.AntInputArtifactObject for JaCoCo coverage */
    @Test fun test_GitDiffUpToDateObject_AntInputArtifactObject() {
        val obj = GitDiffUpToDateObject.AntInputArtifactObject(taskName, propertyName, inputFiles, artifactTaskName)

        assertEquals(taskName, obj.taskName)
        assertEquals(propertyName, obj.propertyName)
        assertEquals(inputFiles, obj.inputFilesOrFolders)
        assertEquals(artifactTaskName, obj.artifactTaskName)
    }


    /** 3) Tests the GitDiffUpToDateObject.GradleInputOutputObject for JaCoCo coverage */
    @Test fun test_GitDiffUpToDateObject_GradleInputOutputObject() {
        val obj = GitDiffUpToDateObject.GradleInputOutputObject(taskName, inputFiles, outputFiles)

        assertEquals(taskName, obj.taskName)
        assertEquals(inputFiles, obj.inputFilesOrFolders)
        assertEquals(outputFiles, obj.outputFilesOrFolders)
    }


    /** 4) Tests the GitDiffUpToDateObject.GradleInputArtifactObject for JaCoCo coverage */
    @Test fun test_GitDiffUpToDateObject_GradleInputArtifactObject() {
        val obj = GitDiffUpToDateObject.GradleInputArtifactObject(taskName, inputFiles, artifactTaskName)

        assertEquals(taskName, obj.taskName)
        assertEquals(inputFiles, obj.inputFilesOrFolders)
        assertEquals(artifactTaskName, obj.artifactTaskName)
    }
}
