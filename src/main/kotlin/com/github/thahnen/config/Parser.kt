package com.github.thahnen.config

import org.gradle.api.Project

import com.github.thahnen.GitDiffUpToDateObject
import com.github.thahnen.PropertyContentInvalidException


/**
 *  Parser:
 *  ======
 *
 *  @author thahnen
 */
internal class Parser {

    companion object {
        /**
         *  Parses the configuration property used by this plugin to set of AntInputOutputObject
         *  -> <Task-Name> : <Ant UpToDate-Property> : <Input files>, ... : <Output files>, ...
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param config necessary property entry
         *  @return set of AntInputOutputObjects parsed from configuration provided
         *  @throws PropertyContentInvalidException when a config is incorrect
         */
        @Throws(PropertyContentInvalidException::class)
        internal fun parseAntSimpleConfig(target: Project, config: String) : Set<GitDiffUpToDateObject.AntInputOutputObject> {
            val taskConfigurations: MutableSet<GitDiffUpToDateObject.AntInputOutputObject> = mutableSetOf()

            config.replace(" ", "").split(";").forEach {
                if (it.isBlank()) {
                    target.logger.warn(
                        "[GitDiffUpToDatePlugin -> ${this@Companion::class.simpleName}." +
                        "${this@Companion::parseAntSimpleConfig.name}] Empty configuration found in " +
                        "gradle.properties file (property: '${GradleProperties.antConfigSimple}') of project " +
                        "'${target.name}'! This does not break the build but is indeed a code smell!"
                    )

                    return@forEach
                }

                with (it.split(",")) {
                    when {
                        this.size != 4 -> throw PropertyContentInvalidException(
                            "[GitDiffUpToDatePlugin -> ${this@Companion::class.simpleName}." +
                            "${this@Companion::parseAntSimpleConfig.name}] Simple config '$it' provided in " +
                            "gradle.properties file (property: '${GradleProperties.antConfigSimple}') of project " +
                            "'${target.name}' is incorrect! Any config must follow the scheme: <Task-Name> : " +
                            "<Ant UpToDate-Property> : <Input files>, ... : <Output files> ..."
                        )
                    }

                    taskConfigurations.add(
                        GitDiffUpToDateObject.AntInputOutputObject(
                            this[0],
                            this[1],
                            this[2].split(",").map { fileOrFolder -> fileOrFolder.trim() }.toSet(),
                            this[3].split(",").map { fileOrFolder -> fileOrFolder.trim() }.toSet()
                        )
                    )
                }
            }

            return taskConfigurations
        }


        /**
         *  Parses the configuration property used by this plugin to set of AntInputArtifactObject
         *  -> <Task-Name> : <Ant UpToDate-Property> : <Input files>, ... : <Gradle Artifact Task-Name>
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param config necessary property entry
         *  @return set of AntInputArtifactObject parsed from configuration provided
         *  @throws PropertyContentInvalidException when a config is incorrect
         */
        @Throws(PropertyContentInvalidException::class)
        internal fun parseAntAdvancedConfig(target: Project, config: String) : Set<GitDiffUpToDateObject.AntInputArtifactObject> {
            val taskConfigurations: MutableSet<GitDiffUpToDateObject.AntInputArtifactObject> = mutableSetOf()

            config.replace(" ", "").split(";").forEach {
                if (it.isBlank()) {
                    target.logger.warn(
                        "[GitDiffUpToDatePlugin -> ${this@Companion::class.simpleName}." +
                        "${this@Companion::parseAntAdvancedConfig.name}] Empty configuration found in " +
                        "gradle.properties file (property: '${GradleProperties.antConfigAdvanced}') of project " +
                        "'${target.name}'! This does not break the build but is indeed a code smell!"
                    )
                }

                with (it.split(",")) {
                    when {
                        this.size != 4 -> throw PropertyContentInvalidException(
                            "[GitDiffUpToDatePlugin -> ${this@Companion::class.simpleName}." +
                            "${this@Companion::parseAntAdvancedConfig.name}] Advanced config '$it' provided in " +
                            "gradle.properties file (property: '${GradleProperties.antConfigAdvanced}') of project " +
                            "'${target.name}' is incorrect! Any config must follow the scheme: <Task-Name> : " +
                            "<Ant UpToDate-Property> : <Input files>, ... : <Gradle Artifact Task-Name>"
                        )
                    }

                    taskConfigurations.add(
                        GitDiffUpToDateObject.AntInputArtifactObject(
                            this[0],
                            this[1],
                            this[2].split(",").map { fileOrFolder -> fileOrFolder.trim() }.toSet(),
                            this[3]
                        )
                    )
                }
            }

            return taskConfigurations
        }


        /**
         *  Parses the configuration property used by this plugin to set of GradleInputOutputObject
         *  -> <Gradle Task-Name> : <Input files>, ... : <Output files>, ...
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param config necessary property entry
         *  @return set of GradleInputOutputObjects parsed from configuration provided
         *  @throws PropertyContentInvalidException when a config is incorrect
         */
        @Throws(PropertyContentInvalidException::class)
        internal fun parseGradleSimpleConfig(target: Project, config: String) : Set<GitDiffUpToDateObject.GradleInputOutputObject> {
            val taskConfigurations: MutableSet<GitDiffUpToDateObject.GradleInputOutputObject> = mutableSetOf()

            config.replace(" ", "").split(";").forEach {
                if (it.isBlank()) {
                    target.logger.warn(
                        "[GitDiffUpToDatePlugin -> ${this@Companion::class.simpleName}." +
                        "${this@Companion::parseGradleSimpleConfig.name}] Empty configuration found in " +
                        "gradle.properties file (property: '${GradleProperties.gradleConfigSimple}') of project " +
                        "'${target.name}'! This does not break the build but is indeed a code smell!"
                    )

                    return@forEach
                }

                with (it.split(",")) {
                    when {
                        this.size != 3 -> throw PropertyContentInvalidException(
                            "[GitDiffUpToDatePlugin -> ${this@Companion::class.simpleName}." +
                            "${this@Companion::parseGradleSimpleConfig.name}] Simple config '$it' provided in " +
                            "gradle.properties file (property: '${GradleProperties.gradleConfigSimple}') of project " +
                            "'${target.name}' is incorrect! Any config must follow the scheme: <Gradle Task-Name> : " +
                            "<Input files>, ... : <Output files> ..."
                        )
                    }

                    taskConfigurations.add(
                        GitDiffUpToDateObject.GradleInputOutputObject(
                            this[0],
                            this[1].split(",").map { fileOrFolder -> fileOrFolder.trim() }.toSet(),
                            this[2].split(",").map { fileOrFolder -> fileOrFolder.trim() }.toSet()
                        )
                    )
                }
            }

            return taskConfigurations
        }


        /**
         *  Parses the configuration property used by this plugin to set of GradleInputArtifactObject
         *  -> <Gradle Task-Name> : <Input files>, ... : <Gradle Artifact Task-Name>
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param config necessary property entry
         *  @return set of GradleInputArtifactObject parsed from configuration provided
         *  @throws PropertyContentInvalidException when a config is incorrect
         */
        @Throws(PropertyContentInvalidException::class)
        internal fun parseGradleAdvancedConfig(target: Project, config: String) : Set<GitDiffUpToDateObject.GradleInputArtifactObject> {
            val taskConfigurations: MutableSet<GitDiffUpToDateObject.GradleInputArtifactObject> = mutableSetOf()

            config.replace(" ", "").split(";").forEach {
                if (it.isBlank()) {
                    target.logger.warn(
                        "[GitDiffUpToDatePlugin -> ${this@Companion::class.simpleName}." +
                        "${this@Companion::parseGradleAdvancedConfig.name}] Empty configuration found in " +
                        "gradle.properties file (property: '${GradleProperties.gradleConfigAdvanced}') of project " +
                        "'${target.name}'! This does not break the build but is indeed a code smell!"
                    )
                }

                with (it.split(",")) {
                    when {
                        this.size != 3 -> throw PropertyContentInvalidException(
                            "[GitDiffUpToDatePlugin -> ${this@Companion::class.simpleName}." +
                            "${this@Companion::parseGradleAdvancedConfig.name}] Advanced config '$it' provided in " +
                            "gradle.properties file (property: '${GradleProperties.gradleConfigAdvanced}') of " +
                            "project '${target.name}' is incorrect! Any config must follow the scheme: " +
                            "<Gradle Task-Name> : <Input files>, ... : <Gradle Artifact Task-Name>"
                        )
                    }

                    taskConfigurations.add(
                        GitDiffUpToDateObject.GradleInputArtifactObject(
                            this[0],
                            this[1].split(",").map { fileOrFolder -> fileOrFolder.trim() }.toSet(),
                            this[2]
                        )
                    )
                }
            }

            return taskConfigurations
        }
    }
}
