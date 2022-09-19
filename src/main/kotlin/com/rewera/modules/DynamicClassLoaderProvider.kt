package com.rewera.modules

import com.google.inject.Inject
import com.google.inject.Provider
import com.rewera.DynamicClassLoader
import io.ktor.server.config.*
import java.net.URL
import java.nio.file.Paths

class DynamicClassLoaderProvider @Inject constructor(config: ApplicationConfig) : Provider<DynamicClassLoader> {

    private val classLoader: DynamicClassLoader by lazy {
        val jarFilePaths = config.property("workflows.jars.paths").getList()
        val urls = jarFilePaths.map { toUrl(it) }.toTypedArray()

        DynamicClassLoader(urls, ClassLoader.getSystemClassLoader())
    }

    private fun toUrl(jarFilePath: String): URL = Paths.get(jarFilePath).toRealPath().toUri().toURL()

    override fun get(): DynamicClassLoader = classLoader

}