package dsl

import com.autodsl.annotation.AutoDsl

@AutoDsl
data class Changelog(var changesets: List<ChangeSet> = mutableListOf())



