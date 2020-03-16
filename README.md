![logo](./doc/updatarium_full.png)
# Updatarium

[![Releases](https://img.shields.io/github/v/release/saagie/updatarium?color=blue)][release]
[![Bintray](https://img.shields.io/bintray/v/saagie/maven/updatarium)][bintray]

[![Issues](https://img.shields.io/github/issues-raw/saagie/updatarium?color=blue)][issues]
[![License](https://img.shields.io/github/license/saagie/updatarium?color=lightgray)][license]
[![Contributors](https://img.shields.io/github/contributors/saagie/updatarium?color=lightgray)][contributors]  

[![workflow github](https://img.shields.io/github/workflow/status/saagie/updatarium/Build%20Master%20and%20Release)][build_master][![Coveralls github](https://img.shields.io/coveralls/github/saagie/updatarium)][coveralls]

[release]: https://github.com/saagie/updatarium/releases
[license]: https://github.com/saagie/updatarium/blob/master/LICENSE
[contributors]: https://github.com/saagie/updatarium/graphs/contributors
[issues]: https://github.com/saagie/updatarium/issues
[bintray]: https://bintray.com/saagie/maven/updatarium/_latestVersion
[coveralls]: https://coveralls.io/github/saagie/updatarium
[build_master]: https://github.com/saagie/updatarium/actions?query=workflow%3A%22Build+Master+and+Release%22
### Goal

The goal of this project is to provide an easy way to execute actions only if it was never executed before. 
It was inspired from liquibase mechanism, but instead of using XML files, we chose to use DSL and Kotlin script files.

### How to use it / Usage

This project generates some libs, so you just have to add them in your `pom.xml` or `build.gradle.kts`: 

#### Maven

In your `pom.xml`:

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

You can also add some engines (example with `engine-httpclient`) or persist-engine

```xml
<dependencies>
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
</dependencies>
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

You can also add some engines (example with `engine-httpclient`) or persist-engine

```kotlin
implementation("io.saagie.updatarium:engine-httpclient:LATEST_VERSION")
implementation("io.saagie.updatarium:persist-mongodb:LATEST_VERSION")
```

#### Running a changeLog

You need to create an Updatarium instance, then call the `executeChangeLog` function with a `Path` of your changeLog file (or a `Reader`): 

```kotlin
Updatarium().executeChangeLog(pathOfYourChangeLogFile)
```

You can also use a persist-engine (to store executions and logs ... see below to know more about persist-engines):  
```kotlin
Updatarium(MongodbPersistEngine()).executeChangeLog(pathOfYourChangeLogFile)
```


#### Running multiples changeLogs 

You can also run some changeLogs, using this function `executeChangeLogs` (`resourcesDirectory` is a Path - needs to be a directory,
and the second arguments is a regex pattern to select changeLogs files): 

```kotlin
Updatarium().executeChangeLogs(resourcesDirectory, "changeLog(.*).kts")
```
 
#### The tag system

You can add some tags into a changeSet like this : 

```kotlin
changeSet(id = "ChangeSet-bash-1-1", author = "Bash") {
    tags = listOf("before")
    action {
        (1..5).forEach {
            logger.info { "Hello $it!" }
        }
    }
}
``` 

And you can `executeChangeLog`(s) with a list of tag. If none, no tag matching system is applied...
If you add a list of tags, all changeSets matched with at least one tag you use will be executed.  

In this example, `ChangeSet-bash-1-1` will be executed. 

 ```kotlin
Updatarium().executeChangeLog(changeLog,listOf("before","after")) 
```

In this example, `ChangeSet-bash-1-1` will not be executed.

```kotlin
Updatarium().executeChangeLog(changeLog,listOf("after")) 
```

#### For the execution of a change set
By default, a changeSet can not be re-executed if it has already been run, based on the changeSet id.

```kotlin
changeLog {
    changeSet(id = "ChangeSet-1", author = "author") {
        action {
            logger.info { "Hello world!" }
        }
    }

    // The following changeSet will not be executed again
    changeSet(id = "ChangeSet-1", author = "author") {
        action {
            logger.info { "Will not be executed" }
        }
    }
}
```

However, it is possible to override this default behaviour by using the force parameter on a specific changeSet:

```kotlin
changeLog {
    changeSet(id = "ChangeSet-1", author = "author") {
        action {
            logger.info { "Hello world!" }
        }
    }

    // The following changeSet will be executed again
    changeSet(id = "ChangeSet-1", author = "author") {
        force = true
        action {
            logger.info { "Hello world again!" }
        }
    }
}
```

#### PersistConfiguration

You can configure the persistEngine, using a `PersistConfiguration` like this : 

```kotlin
val config = PersistConfig(
            level = Level.INFO,
            onSuccessStoreLogs = true,
            onErrorStoreLogs = true
        ) { event -> event.message!! }
Updatarium(MongodbPersistEngine(config)).executeChangeLog(pathOfYourChangeLogFile)
```

A `PersistConfig` instance should have :
 
 - level : org.apache.logging.log4j.Level (the minimal log level captured)
 - onSuccessStoreLogs : boolean. At true, persistEngine will receive a list of logs when call the `unlock` function in case of success.
 - onErrorStoreLogs : boolean. At true, persistEngine will receive a list of logs when call the `unlock` function in case of failure.
 - layout : a lambda representing the transformation to applied to map a `InMemoryEvent` into a `String`
 
### Architecture

#### The concept of ChangeLogs and ChangeSets?

A changeLog represent all changes you have to execute for a version, a date,...

In a changeLog, you can have one or more changeSets. 

Each changeSet represent a list of dependent changes to execute.

One example :
for a new release you need to update all customer documents in a MongoDb database by adding a new field, then call an HTTP endpoint to activate a feature. 
And for the same release, you should do some modifications in Kubernetes pods with no link with the customers documents modification.
You have 2 changeSets: 
 - one for the customer documents and HTTP call
 - one for the Kubernetes modification  

because they are no links between both, but you have a link between the MongoDb document update, and the HTTP call ...
If the MongoDb update failed, you should not execute the HTTP call.

So you'll have this : 

- ChangeLog
   - ChangeSet1 
     - action: update MongoDb
     - action: HTTP call
   - ChangeSet2
     - action: Kubernetes modification
 
#### Internal organization

By design, we decide to split all engines to have a light library.

- ##### core

This project contains all the main code for Updatarium to run.
It contains the changeLog/changeSet model, and the mechanism for the execution and all necessary Interface (`Action` and `PersistEngine`)   

To have a running project, you have a basic implementation of Action with a lambda, and a basic persist implementation (without persistence ... That means all changeSet will be executed)

You have an entry point : the class `Updatarium` with these functions `executeChangeLog()`.   

- ##### persist-*

These projects contain an implementation of a `PersistEngine`.

For the moment, only MongoDb is supported, but you can easily create your own `persist-XXX` project (see the CONTRIBUTING.md for more information) 

- ##### engine-*

These projects contain an implementation of an `Action`.

For the moment : 
- MongoDb
- Bash
- HttpClient
- Kubernetes
are supported, but you can easily create your own `engine-XXX` project (see the CONTRIBUTING.md for more information

- ##### samples

You'll find in the sample directory some examples to use Updatarium.

- ##### updatarium-cli

A command line module to provide an all-in-one application a ship it into a Docker image ([saagie/updatarium](https://hub.docker.com/r/saagie/updatarium)))

### Credits
Logo : 
 - Made by [@pierreLeresteux](https://github.com/pierreLeresteux)
 - Rocket : Created by [Gregor Cresnar](https://thenounproject.com/grega.cresnar/) from noun project 
 - Font : Moon of Jupyter by Frederik (fthafm.com) [https://www.dafont.com/moon-of-jupiter.font](https://www.dafont.com/profile.php?user=982187) 
 
