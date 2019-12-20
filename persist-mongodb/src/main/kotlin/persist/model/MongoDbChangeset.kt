package persist.model

import dsl.ChangeSet
import dsl.Status

data class MongoDbChangeset(val changesetId: String, val author: String, val status : String)

fun ChangeSet.toMongoDbDocument() = MongoDbChangeset(changesetId=this.id, author = this.author, status = Status.EXECUTE.name)