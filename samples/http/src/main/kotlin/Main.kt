import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import dsl.Changelog
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val scriptReader = Files.newBufferedReader(Paths.get("/home/pierre/sources/github/poc_multi/samples/http/src/main/kotlin/changelog.kts"))
    val loadedObj: Changelog = KtsObjectLoader().load<Changelog>(scriptReader)
    loadedObj.changesets.forEach {
        it.execute()
    }
}