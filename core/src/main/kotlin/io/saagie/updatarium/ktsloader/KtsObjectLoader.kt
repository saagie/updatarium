package io.saagie.updatarium.ktsloader

import java.io.InputStream
import java.io.Reader
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * Copied from https://github.com/s1monw1/KtsRunner/blob/master/lib/src/main/kotlin/de/swirtz/ktsrunner/objectloader/KtsObjectLoader.kt
 * since this library is no more available from bintray.
 * This class is not thread-safe, don't use it for parallel executions and create new instances instead.
 */
class KtsObjectLoader(classLoader: ClassLoader? = Thread.currentThread().contextClassLoader) {

    val engine: ScriptEngine = ScriptEngineManager(classLoader).getEngineByExtension("kts")

    inline fun <reified T> Any?.castOrError(): T = takeIf { it is T }?.let { it as T }
        ?: throw IllegalArgumentException("Cannot cast $this to expected type ${T::class}")

    inline fun <reified T> load(script: String): T =
        kotlin.runCatching { engine.eval(script) }
            .getOrElse { throw LoadException("Cannot load script", it) }
            .castOrError()

    inline fun <reified T> load(reader: Reader): T =
        kotlin.runCatching { engine.eval(reader) }
            .getOrElse { throw LoadException("Cannot load script", it) }
            .castOrError()

    inline fun <reified T> load(inputStream: InputStream): T = load(inputStream.reader())

    inline fun <reified T> loadAll(vararg inputStream: InputStream): List<T> = inputStream.map(::load)

}

class LoadException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
