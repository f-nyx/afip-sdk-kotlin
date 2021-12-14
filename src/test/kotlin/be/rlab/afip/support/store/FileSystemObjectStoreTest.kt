package be.rlab.afip.support.store

import be.rlab.afip.auth.model.Credentials
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class FileSystemObjectStoreTest {
    private val storeDir: File by lazy {
        createTempDirectory("test-store-").toFile()
    }

    @BeforeEach
    fun setUp() {
        storeDir.mkdirs()
    }

    @AfterEach
    fun tearDown() {
        storeDir.deleteRecursively()
    }

    @Test
    fun storeAndRead() {
        val store = FileSystemObjectStore(storeDir)
        val data = Credentials("service", "token", "sign", 1234L, "source", "destination", 1234L, 1234L)
        assert(store.save("test1", data, emptyMap()) == data)
        assert(store.exists("test1"))
        assert(store.read<Credentials>("test1")?.content == data)
    }
}
