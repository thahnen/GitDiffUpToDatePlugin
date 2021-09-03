package com.github.thahnen

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.jar.JarInputStream
import java.util.jar.Manifest


/**
 *  JarHandler:
 *  ==========
 *
 *  Class to handle interaction with Jar, mainly Manifest file
 *
 *  @author thahnen
 */
open class JarHandler(jarFile: File) {

    // local variable containing given Jar archives Manifest file
    private var manifest: Manifest


    companion object {
        // manifest attributes
        internal val attributeCommitHash                = "Commit-Hash"
        internal val attributeFileOrFolderHashTemplate  = "hash.FOFHASH"
    }


    /**
     *  primary constructor of class JarHandler
     *
     *  @param jarFile file object of jar file
     *  @throws JarFileNotFoundException when Jar file cannot be found
     *  @throws JarFileIOException when IO exception occurs or Manifest is null
     */
    init {
        try {
            this.manifest = JarInputStream(FileInputStream(jarFile)).manifest ?: throw IOException("")
        } catch (ignored: FileNotFoundException) {
            throw JarFileNotFoundException("Jar file $jarFile cannot be found!")
        } catch (ignored: IOException) {
            throw JarFileIOException("An IO exception occurred when reading Manifest from $jarFile")
        }
    }


    /**
     *  Returns the value of the attribute "Commit-Hash" from Jar Manifest
     *
     *  @return the value of attribute or null
     */
    internal fun getCommitHash() : String? {
        return this.manifest.mainAttributes.getValue(attributeCommitHash)
    }


    /**
     *  Tries to find the commit hash of a given file or folder inside Manifest file
     *
     *  @param fileOrFolder relative path to file or folder to find hash in Manifest file
     *  @return the value of the attribute or null
     */
    @Suppress("unused")
    internal fun getFileOrFolderHash(fileOrFolder: String) : String? {
        return this.manifest.mainAttributes.getValue(
            attributeFileOrFolderHashTemplate.replace("FOFHASH", SHA256.hash(fileOrFolder))
        )
    }
}
