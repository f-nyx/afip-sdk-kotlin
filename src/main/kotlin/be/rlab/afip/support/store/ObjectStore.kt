package be.rlab.afip.support.store

/** General-purpose object store.
 * The implementation decides the storage device and format.
 * It provides consistent read and write. Implementations must be thread-safe.
 */
interface ObjectStore {
    /** Reads an item.
     * @param id Id of the required item.
     * @return the required item, or null if it doesn't exist.
     */
    fun<T : Any> read(id: String): Item<T>?

    /** Saves an item with metadata.
     * @param id Id of the item to store.
     * @param data Item data.
     * @param metadata Optional metadata associated to the item.
     * @return the item data.
     */
    fun<T: Any> save(
        id: String,
        data: T,
        metadata: Map<String, Any?>
    ): T

    /** Saves an item.
     * @param id Id of the item to store.
     * @param data Item data.
     * @return the item data.
     */
    fun<T: Any> save(
        id: String,
        data: T
    ): T

    /** Checks if an object exists in this store.
     * @param id Id of the object to verify.
     * @return true if the object exists, false otherwise.
     */
    fun exists(id: String): Boolean
}
