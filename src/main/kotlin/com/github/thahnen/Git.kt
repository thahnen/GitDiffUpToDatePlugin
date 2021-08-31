package com.github.thahnen

import java.io.File
import java.io.IOException


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
         *  Runs "git rev-parse --verify HEAD" to get the current commit hash
         *
         *  @param dir path were "git rev-parse" should be run
         *  @return commit hash
         *  @throws IOException when error occurs when invoking command
         */
        @Throws(IOException::class)
        fun hash(dir: File) : String = "git rev-parse --verify HEAD".invokeCommandWithOutput(dir)


        /**
         *  Runs "git diff --exit-code <file or folder>" on path provided
         *
         *  @param dir path were "git diff" should be run
         *  @param fileOrFolder which file or folder should be checked on differences
         *  @return true if no difference found, false otherwise
         *  @throws IOException when error occurs when invoking command
         */
        @Throws(IOException::class)
        fun diff(dir: File, fileOrFolder: String) : Boolean = "git diff --exit-code $fileOrFolder".invokeCommand(dir)


        /**
         *  Runs "git log -n 1 --pretty=format:%H -- <file or folder>" on path provided
         *
         *  @param dir path were "git log" should be run
         *  @param fileOrFolder on which file or folder the last commit hash should be returned
         *  @return commit hash
         *  @throws IOException when error occurs when invoking command
         *
         *  TODO: When file is added before commit this string is empty!
         */
        @Throws(IOException::class)
        fun commit(dir: File, fileOrFolder: String) : String {
            return "git log -n 1 --pretty=format:%H -- $fileOrFolder".invokeCommandWithOutput(dir)
        }


        /**
         *  Runs "git merge-base --is-ancestor <parent commit> <child commit>"
         *  Tries to evaluate if <parent commit> was earlier in line than <child commit>. To check if commit hashes are
         *  in different branches with "no" interaction you can run:
         *
         *  if (Git.isParentCommit(parent, child) != 0 && Git.isParentCommit(child, parent) != 0) {
         *      println("Commits $parent / $child are siblings!")
         *  }
         *
         *  @param dir path were "git merge-base" should be run
         *  @param possibleParent commit hash which ist the possible parent of possibleChild
         *  @param possibleChild commit hash which is the possible child of possibleParent
         *  @return true when parent of child, false otherwise
         *  @throws IOException when error occurs when invoking command
         */
        @Throws(IOException::class)
        fun isParentCommit(dir: File, possibleParent: String, possibleChild: String) : Boolean {
            return "git merge-base --is-ancestor $possibleParent $possibleChild".invokeCommand(dir)
        }
    }
}
