## Bash engine

There are multiple ways to trigger a bash action.

### Embedded bash in the changelog
```kotlin
changeLog {
    changeSet(id = "ChangeSet-bash-1", author = "Bash") {
        bashAction(workingDir = "/tmp") {
            """
                echo "pwet"
            """.trimIndent()
        }
    }
}
```

### External .sh file
```kotlin
changeLog {
    changeSet(id = "ChangeSet-bash-1", author = "Bash") {
        bashAction(java.io.File("/path/to/file.sh"))
    }
}
```