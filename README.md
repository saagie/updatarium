![logo](./doc/updatarium_full.png)
# Updatarium

[![Releases](https://img.shields.io/github/v/release/pierreleresteux/updatarium?color=blue)][release]
[![Issues](https://img.shields.io/github/issues-raw/PierreLeresteux/updatarium?color=blue)][issues]
[![License](https://img.shields.io/github/license/PierreLeresteux/updatarium?color=lightgray)][license]
[![Contributors](https://img.shields.io/github/contributors/PierreLeresteux/updatarium?color=lightgray)][contributors]

[release]: https://github.com/PierreLeresteux/updatarium/releases
[license]: https://github.com/PierreLeresteux/updatarium/blob/master/LICENSE
[contributors]: https://github.com/PierreLeresteux/updatarium/graphs/contributors
[issues]: https://github.com/PierreLeresteux/updatarium/issues
### Goal

The goal of this project is to provide an easy way to execute actions only if it was never executed before. It was inspired from liquibase mecanism, but instead of using XML files, we chose to use DSL and Kotlin script files.

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

All these projects contains some changelog examples "How to use this engine" 

### Credits
Logo : 
 - Made by [@PierreLeresteux](https://github.com/PierreLeresteux)
 - Rocket : Created by [Gregor Cresnar](https://thenounproject.com/grega.cresnar/) from noun project 
 - Font : Moon of Jupyter by Frederik (fthafm.com) [https://www.dafont.com/moon-of-jupiter.font](https://www.dafont.com/profile.php?user=982187) 
 
