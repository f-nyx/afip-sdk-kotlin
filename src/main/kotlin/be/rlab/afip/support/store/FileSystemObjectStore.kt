package be.rlab.afip.support.store

import be.rlab.afip.support.ObjectMapperFactory
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.InputStream

/** General purpose object store that stores data in the file system as JSON.
 *
 * This implementation uses Jackson for serialization. If you don't want to use Jackson,
 * it is possible to exclude the dependency and override the [serialize] and [deserialize] methods.
 *
 * This is thread safe.
 *
 * @param storageDir Directory to store data.
 */
open class FileSystemObjectStore(private val storageDir: File) : ObjectStore {
    init {
        require(!storageDir.exists() || (storageDir.exists() && storageDir.isDirectory)) { "Invalid directory" }
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }

    override fun exists(id: String): Boolean {
        return fileFor(id).exists()
    }

    @Suppress("UNCHECKED_CAST")
    override fun<T : Any> read(id: String): Item<T>? {
        val file = fileFor(id)
        return if (file.exists()) {
            file.inputStream().use { fileIn ->
                deserialize(fileIn) as Item<T>
            }
        } else {
            null
        }
    }

    override fun <T : Any> save(
        id: String,
        data: T
    ): T {
        return save(id, data, emptyMap())
    }

    override fun<T : Any> save(
        id: String,
        data: T,
        metadata: Map<String, Any?>
    ): T = synchronized(this) {
        val item = Item(id, data, metadata)
        fileFor(item.id).outputStream().use { fileOut ->
            serialize(item).byteInputStream().transferTo(fileOut)
        }
        return data
    }

    protected open fun deserialize(data: InputStream): Item<*> {
        return ObjectMapperFactory.anySupportMapper.readValue(data)
    }

    protected open fun serialize(item: Item<*>): String {
        return ObjectMapperFactory.anySupportMapper.writeValueAsString(item)
    }

    private fun fileFor(serviceName: String): File {
        return File(storageDir, serviceName)
    }
}
