![logo](./doc/updatarium_full.png)
# Updatarium

[![Releases](https://img.shields.io/github/v/release/saagie/updatarium?color=blue)][release]
[![Bintray](https://img.shields.io/bintray/v/saagie/maven/updatarium)][bintray]

[![Issues](https://img.shields.io/github/issues-raw/saagie/updatarium?color=blue)][issues]
[![License](https://img.shields.io/github/license/saagie/updatarium?color=lightgray)][license]
[![Contributors](https://img.shields.io/github/contributors/saagie/updatarium?color=lightgray)][contributors]

[release]: https://github.com/saagie/updatarium/releases
[license]: https://github.com/saagie/updatarium/blob/master/LICENSE
[contributors]: https://github.com/saagie/updatarium/graphs/contributors
[issues]: https://github.com/saagie/updatarium/issues
[bintray]: https://bintray.com/saagie/maven/updatarium/_latestVersion
### Goal

The goal of this project is to provide an easy way to execute actions only if it was never executed before. It was inspired from liquibase mecanism, but instead of using XML files, we chose to use DSL and Kotlin script files.

### How to use it / Usage

This project generate some libs, so you just have to add them in your pom.xml or build.gradle.kts: 

#### Maven

In your pom.xml:

Add JCenter maven repository 
```xml
    <repositories>
        <repository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>central</id>
            <name>bintray-plugins</name>
            <url>https://jcenter.bintray.com</url>
        </repository>
    </repositories>
```
And add our libs (core at least) replacing `updatarium.version` with the latest version

```xml
    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>io.saagie.updatarium</groupId>
            <artifactId>core</artifactId>
            <version>${updatarium.version}</version>
        </dependency>
    </dependencies>
```

You can also add some engines (example with `engine-httpclient`) or persit-engine
```xml
    <dependency>
        <groupId>io.saagie.updatarium</groupId>
        <artifactId>engine-httpclient</artifactId>
        <version>${updatarium.version}</version>
    </dependency>
    <dependency>
        <groupId>io.saagie.updatarium</groupId>
        <artifactId>persist-mongodb</artifactId>
        <version>${updatarium.version}</version>
    </dependency>
```
#### Gradle

Add JCenter maven repository

```kotlin
repositories {
    //...
    jcenter()
}
```
...  
And add our libs (core at least) replacing `LATEST_VERSION` with the latest version
```kotlin
dependencies {
    implementation(kotlin("stdlib-jdk8")) // Kotlin standard libs

    implementation("io.saagie.updatarium:core:LATEST_VERSION") // Updatarium Core library (mandatory)
    
    // Kotlin scripting (mandatory to use kts compilation)
    implementation(kotlin("scripting-compiler-embeddable")) 
    implementation(kotlin("script-util"))
}
```

You can also add some engines (example with `engine-httpclient`) or persit-engine
```kotlin
implementation("io.saagie.updatarium:engine-httpclient:LATEST_VERSION")
implementation("io.saagie.updatarium:persist-mongodb:LATEST_VERSION")
```

#### Running a changelog

You need to create an Updatarium instance, then call the `executeChangelog` function with a Path of your changelog file (or a Reader): 
```kotlin
Updatarium().executeChangelog(pathOfYourChangelogFile)
```

You can also use a persist-engine (to store executions and logs ... see below to know more about persist-engines):  
```kotlin
Updatarium(MongodbPersistEngine()).executeChangelog(pathOfYourChangelogFile)
```


#### Running multiples changelogs 

You can also run some changelogs, using this function `executeChangelogs` (`resourcesDirectory` is a Path - needs to be a directory, and the second arguments is a regex pattern to select changelogs files): 

```kotlin
Updatarium().executeChangelogs(resourcesDirectory, "changelog(.*).kts")
```
 
#### The tag system
You can add some tags into a changeset like this : 
```kotlin
+changeSet {
    id = "ChangeSet-bash-1-1"
    author = "Bash"
    tags = listOf("before")
    actions {
        +BasicAction {
            (1..5).forEach {
                logger.info { "Hello $it!" }
            }

        }
    }
}
``` 

And you can executeChangelog(s) with a list of tag. If none, no tag matching system is applied...   
 If you add a list of tags, all changesets matched with at least one tag you use will be executed.  

In this example, `ChangeSet-bash-1-1` will be executed. 
 ```kotlin
Updatarium().executeChangelog(changelog,listOf("before","after")) 
```
In this example, `ChangeSet-bash-1-1` will not be executed.
```kotlin
Updatarium().executeChangelog(changelog,listOf("after")) 
```

#### PersistConfiguration

You can configure the persistEngine, using a PersitConfiguration like this : 

```kotlin
val config = PersistConfig(
            level = Level.INFO,
            onSuccessStoreLogs = true,
            onErrorStoreLogs = true
        ) { event -> event.message!! }
Updatarium(MongodbPersistEngine(config)).executeChangelog(pathOfYourChangelogFile)
```

A `PersistConfig` instance sould have : 
 - level : org.apache.logging.log4j.Level (the minimal log level captured)
 - onSuccessStoreLogs : boolean. At true, persitEngine will receive a list of logs when call the `unlock` function in case of succes.
 - onErrorStoreLogs : boolean. At true, persitEngine will receive a list of logs when call the `unlock` function in case of failure.
 - layout : a lambda representing the transformation to applied to map a `InMemoryEvent` into a `String`
 
### Architecture

#### The concept of Changelogs and Changesets?

A changelog represent all changes you have to execute for a version, a date,...

In a changelog, you can have one or more changesets. 

Each changeset represent a list of dependent changes to execute.

One example :  
for a new release you need to update all customer documents in a MongoDb database by adding a new field, then call a HTTP endpoint to activate a feature. And for the same release, you should do some modifications in Kubernetes pods with no link with the customers documents modification.  
You have 2 changesets: 
 - one for the customer documents and HTTP call
 - one for the Kubernetes modification  

because they are no links between both, but you have a link between the MongoDb document update and the HTTP call ... If the MongoDb update failed, you should not execute the HTTP call.

So you'll have this : 

- Changelog
   - Changeset1 
     - action: update MongoDb
     - action: HTTP call
   - Changeset2
     - action: Kubernetes modification
 
#### Internal organization

By design, we decide to split all engines to have a light library.

- ##### core
This project contains all the main code for Updatarium to run.  
It contains the changelog/changeset model, and the mecanism for the execution and all necessary Interface (`Action` and `PersistEngine`)   

To have a running project, you have a basic implementation of Action (BasicAction) and a basic persist implementation (without persistence ... That means all changeset will be executed)

You have an entrypoint : the class `Updatarium` with these functions `executeChangelog()`.   
- ##### persist-*
These projects contains an implementation of a `PersistEngine`.

For the moment, only MongoDb is supported, but you can easily create your own `persist-XXX` project (see the CONTRIBUTING.md for more informations) 
- ##### engine-*
These projects contains an implementation of an `Action`.

For the moment : 
- MongoDb
- Bash
- HttpClient
- Kubernetes 
are supported, but you can easily create your own `engine-XXX` project (see the CONTRIBUTING.md for more informations
- ##### samples

You'll find in the sample directory some examples to use Updatarium.
### Credits
Logo : 
 - Made by [@pierreLeresteux](https://github.com/pierreLeresteux)
 - Rocket : Created by [Gregor Cresnar](https://thenounproject.com/grega.cresnar/) from noun project 
 - Font : Moon of Jupyter by Frederik (fthafm.com) [https://www.dafont.com/moon-of-jupiter.font](https://www.dafont.com/profile.php?user=982187) 
 
