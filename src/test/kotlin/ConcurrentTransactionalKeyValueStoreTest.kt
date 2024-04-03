import com.antonshillov.transactionalkv.store.ConcurrentTransactionalKeyValueStore
import com.antonshillov.transactionalkv.store.TransactionalKeyValueStore
import com.antonshillov.transactionalkv.util.ConsoleLogger
import kotlin.test.*

class ConcurrentTransactionalKeyValueStoreTest {
    private lateinit var store: TransactionalKeyValueStore

    @BeforeTest
    fun setUp() {
        store = ConcurrentTransactionalKeyValueStore(logger = ConsoleLogger(enabled = false))
    }

    @Test
    fun `set and get`() {
        store.set("foo", "123")
        assertEquals("123", store.get("foo"), "GET should return the value set by SET.")
    }

    @Test
    fun `delete`() {
        store.set("foo", "123")
        store.delete("foo")
        assertNull(store.get("foo"), "GET should return 'null' after DELETE.")
    }

    @Test
    fun `count`() {
        store.set("foo", "123")
        store.set("bar", "456")
        store.set("baz", "123")
        assertEquals(2, store.count("123"), "COUNT should return the number of keys with the given value.")
        assertEquals(1, store.count("456"), "COUNT should return the number of keys with the given value.")
    }

    @Test
    fun `commit transaction`() {
        store.set("bar", "123")
        store.begin()
        store.set("foo", "456")
        store.delete("bar")
        store.get("bar")
        store.commit()
        assertNull(store.get("bar"), "GET should return 'null' for 'bar' after COMMIT.")
        assertEquals(
            "456",
            store.get("foo"),
            "GET should return the value for 'foo' set inside the transaction after COMMIT."
        )
    }

    @Test
    fun `rollback transaction`() {
        store.set("foo", "123")
        store.begin()
        store.set("foo", "456")
        store.rollback()
        assertEquals("123", store.get("foo"), "GET should return the original value for 'foo' after ROLLBACK.")
    }

    @Test
    fun `nested transactions`() {
        store.set("foo", "123")
        store.begin() // First transaction
        store.set("foo", "456")

        store.begin() // Nested transaction
        store.set("foo", "789")
        store.rollback() // Rollback nested transaction

        assertEquals(
            "456",
            store.get("foo"),
            "GET should return the value from the first transaction after nested transaction ROLLBACK."
        )

        store.rollback() // Rollback first transaction
        assertEquals(
            "123",
            store.get("foo"),
            "GET should return the original value after rolling back the first transaction."
        )
    }

    @Test
    fun `no transaction`() {
        assertFalse(store.commit(), "COMMIT should return false if no transaction is started.")
        assertFalse(store.rollback(), "ROLLBACK should return false if no transaction is started.")
    }

    @Test
    fun `parallel thread transactions`() {
        // Thread for the first transaction
        val transaction1 = Thread {
            store.begin()
            try {
                Thread.sleep(100) // Simulate a long-running transaction
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            store.set("key1", "value1")
            store.commit()
        }

        // Thread for the second transaction
        val transaction2 = Thread {
            try {
                Thread.sleep(50) // Ensure this starts after transaction1 has begun but before it commits
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            store.begin()
            store.set("key1", "value2")
            store.commit()
        }

        // Start both transactions
        transaction1.start()
        transaction2.start()

        // Wait for both to complete
        transaction1.join()
        transaction2.join()

        // Evaluate the final value
        val value1 = store.get("key1")
        assertEquals("value2", value1, "The last committed value should persist.")
    }
}

