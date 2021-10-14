package com.github.thahnen.config

import org.gradle.api.Project


/**
 *  GradleProperties:
 *  ================
 *
 *  Contains all constants describing property names for a projects gradle.properties file
 *
 *  @author thahnen
 */
internal sealed class GradleProperties {

    companion object {
        // base plugin identifier
        private const val base                          = "plugins.gitdiffuptodate"

        // Ant: configuration identifiers
        internal const val antConfigSimple              = "$base.ant.configSimple"
        internal const val antConfigAdvanced            = "$base.ant.configAdvanced"
        internal const val antEvaluateManifest          = "$base.ant.evaluateManifest"
        internal const val antUseFileOrFolderHashes     = "$base.ant.useFileOrFolderHashes"
        internal const val antUseSrcResourcesBlock      = "$base.ant.useSrcResourcesBlock"

        // Gradle: configuration identifiers
        internal const val gradleConfigSimple           = "$base.gradle.configSimple"
        internal const val gradleConfigAdvanced         = "$base.gradle.configAdvanced"
        internal const val gradleEvaluateManifest       = "$base.gradle.evaluateManifest"
        internal const val gradleUseFileOrFolderHashes  = "$base.gradle.useFileOrFolderHashes"
        internal const val gradleSkipTaskGraph          = "$base.gradle.skipTaskGraph"


        /**
         *  Tries to retrieve a properties file entry from local gradle.properties file!
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param propertyKey the key of the property which entry should be retrieved
         *  @return content of properties entry or null if not found
         */
        internal fun getLocalPropertiesEntry(target: Project, propertyKey: String) : String? {
            return when {
                target.properties.containsKey(propertyKey)  -> target.properties[propertyKey]
                else                                        -> null
            } as String?
        }


        /**
         *  Tries to retrieve a properties file entry from local / root project gradle.properties file, system property
         *  or environment variable!
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param propertyKey the key of the property which entry should be retrieved
         *  @return content of properties entry or null if not found
         */
        internal fun getGlobalPropertiesEntry(target: Project, propertyKey: String): String? {
            return when {
                target.properties.containsKey(propertyKey)              -> target.properties[propertyKey]
                target.rootProject.properties.containsKey(propertyKey)  -> target.rootProject.properties[propertyKey]
                System.getProperties().containsKey(propertyKey)         -> System.getProperties()[propertyKey]
                System.getenv().containsKey(propertyKey)                -> System.getenv(propertyKey)
                else                                                    -> null
            } as String?
        }
    }
}
