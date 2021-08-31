# GitDiffUpToDatePlugin

![example workflow](https://github.com/thahnen/UniformDependenciesPlugin/actions/workflows/gradle.yml/badge.svg)
![example workflow](https://github.com/thahnen/UniformDependenciesPlugin/actions/workflows/gradle_validation.yml/badge.svg)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/thahnen/UniformDependenciesPlugin/actions/workflows/gradle.yml)

Configure Gradle tasks of project to be UP-TO-DATE depending on Git diff result of files / folders
provided to this plugin.

## WIP

tasks.getByName("TaskName") { outputs.upToDateWhen { JAR existiert + alle Dateien / Ordner haben keinen Git diff! } }

Angabe ueber Properties irgendwie:

```properties
plugins.gitdiffuptodate.config = \
  <TaskName 1> : Ordner, Datei 1, Datei 2; \
  <TaskName 2> : Datei, Ordner 1, Ordner 2

plugins.gitdiffuptodate.evaluateManifest=Boolean
```

Wenn Manifest Property gesetzt ist, wird überprüft ob muss überprüft werden, ob der Commit-Hash in der Manifest-Datei
älter ist als der Commit-Hash der Dateien oder eine Datei seit dem letzten Commit geändert wurde. Wenn keine Änderungen
vorhanden sind muss auch das JAR nicht neugebaut werden (bzw. die angegebenen Tasks).
Ggf. den Commit-Hash (bzw. Änderungsdatum) der einzelnen Dateien in die Manifest-Datei miteintragen?
