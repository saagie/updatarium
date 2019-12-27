class Main

fun main() {
    MagicalUpdater().executeChangelog(Main::class.java.getResource("changelog.kts").readText())

}
