# Contributing

When contributing to this repository, please first discuss the change you wish to make via issue with the owners of this repository before making a change. 

Please note we have a code of conduct, please follow it in all your interactions with the project.

## Pull Request Process

1. Ensure any code or file you have created during your dev process are really needed before pushing it.
2. Update the README.md with details of changes if needed.
3. When your PR is submitted, owners and maintainers will review the PR and comment or accept it.

## Tools we use to build
- JDK 1.8+
- Gradle 6+ (use ./gradlew[.bat] everytime !!!)
- Kotlin 1.3+
- Github actions (see the .github directory)

## Action creation

The action creation is quite simple.  
You need to create a new Kotlin project and just add the 
`implementation(project("io.saagie.updatarium:core:{latest-version}"))`

Now you can create your own implementation of `Action`.  
We use `com.autodsl.annotation.AutoDsl` annotation to transform our class in a simple DSL, but you can create you own DSL without.

`Action` interface contains only one function : 

```$kotlin
abstract fun execute()
```

in your `execute` function you can implement your own logic. You can see existing `engine-XXX` to see some examples. 

## Persist Engine creation

The persist engine creation is like action creation: quite simple too.  

You need to create a new Kotlin project and just add the 
`implementation(project("io.saagie.updatarium:core:{latest-version}"))`

Now you can create your own implementation of `PersistEngine`.  


`PersistEngine` interface contains four functions : 

```$kotlin
   abstract fun checkConnection()
   abstract fun notAlreadyExecuted(changeSetId: String): Boolean
   abstract fun lock(changeSet: ChangeSet)
   abstract fun unlock(changeSet: ChangeSet, status: Status, logs: List<InMemoryEvent<Level, LogEvent>>)
```

Let's see in details each function : 

- `checkConnection()` is called before starting the changelog execution to check that the persistence is ready (checking a database connection for example). This function returns nothing, you have to throw an exception is the persistence is not ready and it will stop the process.

- `notAlreadyExecuted(changeSetId: String)` is called at the changeset execution beginning. You have to check in your persistence engine (database ? file ? memory ? ) if you have previously executed this changesetId. Return true to allow this changeset execution, false otherwise.

- `lock(changeSet: ChangeSet)` is called just before execute the changeset actions. You have to record the changeset execution in your persistence engine (with a `Status.EXECUTE` status).

- `unlock(changeSet: ChangeSet,status: Status, logs: List<InMemoryEvent<Level, LogEvent>>)` is called a the end of the actions execution for a changeset. You have the correct status (`Status.OK` or `Status.KO`) and an ordered list containing all LogEvent captured during the execution of the changeset, and you can update this new status to the changeset record.

## Code of Conduct

### Our Pledge

In the interest of fostering an open and welcoming environment, we as
contributors and maintainers pledge to making participation in our project and
our community a harassment-free experience for everyone, regardless of age, body
size, disability, ethnicity, gender identity and expression, level of experience,
nationality, personal appearance, race, religion, or sexual identity and
orientation.

### Our Standards

Examples of behavior that contributes to creating a positive environment
include:

* Using welcoming and inclusive language
* Being respectful of differing viewpoints and experiences
* Gracefully accepting constructive criticism
* Focusing on what is best for the community
* Showing empathy towards other community members

Examples of unacceptable behavior by participants include:

* The use of sexualized language or imagery and unwelcome sexual attention or
advances
* Trolling, insulting/derogatory comments, and personal or political attacks
* Public or private harassment
* Publishing others' private information, such as a physical or electronic
  address, without explicit permission
* Other conduct which could reasonably be considered inappropriate in a
  professional setting

### Our Responsibilities

Project maintainers are responsible for clarifying the standards of acceptable
behavior and are expected to take appropriate and fair corrective action in
response to any instances of unacceptable behavior.

Project maintainers have the right and responsibility to remove, edit, or
reject comments, commits, code, wiki edits, issues, and other contributions
that are not aligned to this Code of Conduct, or to ban temporarily or
permanently any contributor for other behaviors that they deem inappropriate,
threatening, offensive, or harmful.

### Scope

This Code of Conduct applies both within project spaces and in public spaces
when an individual is representing the project or its community. Examples of
representing a project or community include using an official project e-mail
address, posting via an official social media account, or acting as an appointed
representative at an online or offline event. Representation of a project may be
further defined and clarified by project maintainers.

### Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be
reported by contacting the project team at updatarium@saagie.com. All
complaints will be reviewed and investigated and will result in a response that
is deemed necessary and appropriate to the circumstances. The project team is
obligated to maintain confidentiality with regard to the reporter of an incident.
Further details of specific enforcement policies may be posted separately.

Project maintainers who do not follow or enforce the Code of Conduct in good
faith may face temporary or permanent repercussions as determined by other
members of the project's leadership.

### Attribution

This Code of Conduct is adapted from the [Contributor Covenant][homepage], version 1.4,
available at [http://contributor-covenant.org/version/1/4][version]

[homepage]: http://contributor-covenant.org
[version]: http://contributor-covenant.org/version/1/4/