package com.github.thahnen.util

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.jar.JarInputStream
import java.util.jar.Manifest

import com.github.thahnen.JarFileIOException
import com.github.thahnen.JarFileNotFoundException


/**
 *  BundleHandler:
 *  =============
 *
 *  Class to handle interaction with JAR/WAR/EAR, mainly manifest file
 *
 *  @author thahnen
 */
internal class BundleHandler(jarFile: File) {

    // local variable containing given Jar archives Manifest file
    private var manifest: Manifest


    // static variables containing manifest attributes
    companion object {
        val attributeCommitHash        = "Commit-Hash"
        val attributeFileHashTemplate  = "hash.FILEHASH"
    }


    /** primary constructor of class JarHandler */
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
    fun getCommitHash() : String? = this.manifest.mainAttributes.getValue(attributeCommitHash)


    /**
     *  Tries to find the commit hash of a given file inside Manifest file
     *
     *  @param file relative path to file to find hash in Manifest file
     *  @return the value of the attribute or null
     */
    @Suppress("unused")
    fun getFileHash(file: String) : String? = this.manifest.mainAttributes.getValue(
        attributeFileHashTemplate.replace(attributeCommitHash.split(".")[1], SHA256.hash(file))
    )
}
