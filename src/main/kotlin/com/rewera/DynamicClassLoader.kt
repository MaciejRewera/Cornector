package com.rewera;

import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths

class DynamicClassLoader(
    urls: Array<URL> = emptyArray(),
    parent: ClassLoader = Thread.currentThread().contextClassLoader
) : URLClassLoader(urls, parent) {

    @Throws(IOException::class)
    fun appendToClassPath(jarFile: String) {
        addURL(Paths.get(jarFile).toRealPath().toUri().toURL())
    }

    companion object {
        init {
            registerAsParallelCapable()
        }
    }
}
