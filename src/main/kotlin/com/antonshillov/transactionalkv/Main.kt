package com.antonshillov.transactionalkv

import com.antonshillov.transactionalkv.store.SynchronousTransactionalKeyValueStore
import com.antonshillov.transactionalkv.util.ConsoleLogger
import java.io.BufferedReader
import java.io.InputStreamReader

fun main() {
    val logger = ConsoleLogger(false)
    val store = SynchronousTransactionalKeyValueStore(logger = logger)
    val app = CliApp(
        store = store,
        inputReader = BufferedReader(InputStreamReader(System.`in`)),
        output = System.out
    )
    app.run()
}

