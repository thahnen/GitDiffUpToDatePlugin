package com.github.thahnen

import java.io.File


/**
 *  Git:
 *  ===
 *
 *  Class to handle git commands in specific directory provided by user
 *
 *  @author thahnen
 */
class Git {

    companion object {
        /**
         *  Runs "git diff --exit-code <file or folder" on path provided
         *
         *  @param dir path were "git diff" should be run
         *  @param fileOrFolder which file or folder should be checked on differences
         *  @return true if no difference found, false otherwise
         *
         *  TODO: Handle exceptions?
         */
        fun diff(dir: File, fileOrFolder: String) : Boolean = "git diff --exit-code $fileOrFolder".invokeCommand(dir)
    }
}
