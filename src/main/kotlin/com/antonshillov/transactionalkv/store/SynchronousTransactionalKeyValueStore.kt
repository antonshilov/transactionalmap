package com.antonshillov.transactionalkv.store

import com.antonshillov.transactionalkv.util.ConsoleLogger
import com.antonshillov.transactionalkv.util.Logger


// Non tread-safe implementation to illustrate difference in concurrent env
class SynchronousTransactionalKeyValueStore(
    private val store: MutableMap<String, String?> = mutableMapOf<String, String?>(),
    private val logger: Logger = ConsoleLogger()
) : TransactionalKeyValueStore {
    private val transactions = ArrayDeque<MutableMap<String, String?>>()

    override fun set(key: String, value: String) {
        getCurrentStore()[key] = value
        logger.log("SET $key = $value")
    }

    override fun get(key: String): String? {
        return getCurrentStore()[key].also { value ->
            logger.log("GET $key = $value")
        }
    }

    override fun delete(key: String) {
        getCurrentStore().remove(key).also {
            logger.log("DELETE $key")
        }
    }

    override fun count(value: String): Int {
        return getCurrentStore().count { it.value == value }.also { count ->
            logger.log("COUNT $value = $count")
        }
    }

    override fun begin() {
        transactions.add(mutableMapOf())
        logger.log("BEGIN transaction")
    }

    override fun commit(): Boolean {
        if (transactions.isEmpty()) {
            logger.log("no transaction")
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
        logger.log("Transaction committed.")
        return true
    }

    override fun rollback(): Boolean {
        if (transactions.isEmpty()) {
            logger.log("no transaction")
            return false
        }
        transactions.removeLast()
        logger.log("Transaction rolled back.")
        return true
    }

    private fun getCurrentStore(): MutableMap<String, String?> {
        return transactions.lastOrNull() ?: store
    }
}
