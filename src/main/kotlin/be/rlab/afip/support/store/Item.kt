package be.rlab.afip.support.store

data class Item<T>(
    val id: String,
    val content: T,
    val metadata: Map<String, Any?>
) {
    companion object {
        fun new(
            id: String,
            content: Any,
            metadata: Map<String, Any?> = mapOf()
        ): Item<*> = Item(id, content, metadata)
    }
}
