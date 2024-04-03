package com.antonshillov.transactionalkv.store

import com.antonshillov.transactionalkv.util.ConsoleLogger
import com.antonshillov.transactionalkv.util.Logger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class ConcurrentTransactionalKeyValueStore(
    private val store: MutableMap<String, String?> = mutableMapOf<String, String?>(),
    private val logger: Logger = ConsoleLogger()
) : TransactionalKeyValueStore {
    // If we are expected to have huge transactions can optimize by storing operations instead of whole Map copy
    // But get and count implementations will have to change for that
    private val transactions = ArrayDeque<MutableMap<String, String?>>()
    private val lock = ReentrantReadWriteLock()

    override fun set(key: String, value: String) {
        lock.write {
            logger.log("SET $key = $value")
            getCurrentStore()[key] = value
        }
    }

    override fun get(key: String): String? = lock.read {
        getCurrentStore()[key].also { value ->
            logger.log("GET $key = $value")
            return value
        }
    }

    override fun delete(key: String) {
        lock.write {
            if (transactions.isNotEmpty()) {
                getCurrentStore()[key] = null
            } else {
                getCurrentStore().remove(key)
            }
            logger.log("DELETE $key")

        }
    }

    // If we are expected to get frequent count calls
    // We can cache count results to avoid frequent O(n) calculations
    override fun count(value: String): Int = lock.read {
        getCurrentStore().count { it.value == value }.also { count ->
            logger.log("COUNT $value = $count")
        }
    }

    override fun begin() {
        //locking the access when starting a transaction. has to be unlocked in rollback & commit
        lock.writeLock().lock()
        logger.log("BEGIN transaction")
        transactions.addLast(mutableMapOf())

    }

    override fun commit(): Boolean = lock.write {
        if (transactions.isEmpty()) {
            logger.log("COMMIT failed: no transaction")
            return false
        }
        val lastTransaction = transactions.removeLast()
        if (transactions.isNotEmpty()) {
            transactions.last().putAll(lastTransaction)
        } else {
            lastTransaction.forEach { (key, value) ->
                if (value == null) store.remove(key) else store[key] = value
            }
        }
        lock.writeLock().unlock()
        logger.log("COMMIT successful")
        return true
    }

    override fun rollback(): Boolean = lock.write {
        if (transactions.isEmpty()) {
            logger.log("ROLLBACK failed: no transaction")
            return false
        }
        transactions.removeLast()
        lock.writeLock().unlock()
        logger.log("ROLLBACK successful")
        return true
    }

    private fun getCurrentStore(): MutableMap<String, String?> {
        return transactions.lastOrNull() ?: store
    }
}
