package com.antonshillov.transactionalkv.util

interface Logger {
    fun log(message: String)
}

class ConsoleLogger(private val enabled: Boolean = true) : Logger {
    override fun log(message: String) {
        if (enabled) println(message)
    }
}
