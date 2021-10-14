package com.github.thahnen.util

import java.security.MessageDigest


/**
 *  SHA256:
 *  ======
 *
 *  Class to generate SHA-256 hash of given string
 *
 *  @author thahnen
 */
sealed class SHA256 {

    companion object {
        /**
         *  Hashes a given string using the SHA-256 hashing function
         *
         *  @param stringToHash string to get hash from
         *  @return hashed string
         */
        fun hash(stringToHash: String) : String {
            return MessageDigest.getInstance("SHA-256")
                .digest(stringToHash.toByteArray())
                .fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}
