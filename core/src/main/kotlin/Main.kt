import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import dsl.Changelog
import persist.engine.MongodbEngine
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val scriptReader = Files.newBufferedReader(Paths.get("/home/pierre/sources/github/poc_multi/core/src/main/resources/changelog.kts"))
    val loadedObj: Changelog = KtsObjectLoader().load<Changelog>(scriptReader)
    loadedObj.execute()
}