package be.rlab.afip.config

import be.rlab.afip.support.store.FileSystemObjectStore
import be.rlab.afip.support.store.InMemoryObjectStore
import be.rlab.afip.support.store.ObjectStore
import java.io.File

class StoreConfig {
    class FileSystemStoreConfig {
        lateinit var storeDir: File
    }

    private lateinit var lazyStore: () -> ObjectStore
    private var store: ObjectStore? = null

    fun fileSystem(callback: FileSystemStoreConfig.() -> Unit) {
        lazyStore = {
            val config = FileSystemStoreConfig().apply(callback)
            FileSystemObjectStore(config.storeDir)
        }
    }

    fun memory() {
        lazyStore = { InMemoryObjectStore() }
    }

    fun build(): ObjectStore {
        if (store == null) {
            store = lazyStore()
        }
        return store!!
    }
}
