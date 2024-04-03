package com.antonshillov.transactionalkv.store

interface TransactionalKeyValueStore {
    fun set(key: String, value: String)
    fun get(key: String): String?
    fun delete(key: String)
    fun count(value: String): Int
    fun begin()
    fun commit(): Boolean
    fun rollback(): Boolean
}
