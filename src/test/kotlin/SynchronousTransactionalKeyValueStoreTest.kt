import com.antonshillov.transactionalkv.store.SynchronousTransactionalKeyValueStore
import com.antonshillov.transactionalkv.store.TransactionalKeyValueStore
import kotlin.test.*

class SynchronousTransactionalKeyValueStoreTest {
    private lateinit var store: TransactionalKeyValueStore

    @BeforeTest
    fun setUp() {
        store = SynchronousTransactionalKeyValueStore()
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
        val value1 = store.get("key1")
        assertEquals("value2", value1)

        //will fail because transaction1 finishes after transaction2
        assertEquals("value2", value1)
    }
}
