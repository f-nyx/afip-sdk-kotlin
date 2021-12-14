package be.rlab.afip.support.store

/** Object store that stores data in memory for tes purposes.
 */
class InMemoryObjectStore : ObjectStore {

    private val storage: MutableMap<String, Item<*>> = mutableMapOf()

    override fun exists(id: String): Boolean {
        return id in storage
    }

    @Suppress("UNCHECKED_CAST")
    override fun<T : Any> read(id: String): Item<T>? {
        return storage[id] as Item<T>?
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
        storage[id] = item
        return data
    }
}
