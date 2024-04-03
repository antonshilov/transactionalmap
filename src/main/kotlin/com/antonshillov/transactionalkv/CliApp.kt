package com.antonshillov.transactionalkv

import com.antonshillov.transactionalkv.store.SynchronousTransactionalKeyValueStore
import java.io.BufferedReader
import java.io.PrintStream

class CliApp(
    private val store: SynchronousTransactionalKeyValueStore,
    private val inputReader: BufferedReader,
    private val output: PrintStream
) {
    fun run() {
        output.println("Welcome to the Transactional Key Value Store CLI")
        while (true) {
            output.print("> ")
            val input = inputReader.readLine()?.trim() ?: break
            if (input.isBlank()) continue

            val parts = input.split("\\s+".toRegex())
            val command = parts[0].uppercase()
            when (command) {
                CMD_SET -> handleSetCommand(parts)
                CMD_GET -> handleGetCommand(parts)
                CMD_DELETE -> handleDeleteCommand(parts)
                CMD_COUNT -> handleCountCommand(parts)
                CMD_BEGIN -> store.begin().also { output.println("Transaction started.") }
                CMD_COMMIT -> if (!store.commit()) output.println(USAGE_COMMIT) else output.println(
                    "Transaction committed."
                )

                CMD_ROLLBACK -> if (!store.rollback()) output.println(USAGE_ROLLBACK) else output.println(
                    "Transaction rolled back."
                )

                CMD_EXIT -> {
                    output.println(MSG_EXITING_CLI)
                    break
                }

                else -> output.println(MSG_UNKNOWN_COMMAND)
            }
        }
    }

    private fun handleSetCommand(parts: List<String>) {
        if (parts.size < 3) {
            output.println(USAGE_SET)
        } else {
            store.set(parts[1], parts.drop(2).joinToString(" "))
            output.println("Value set.")
        }
    }

    private fun handleGetCommand(parts: List<String>) {
        if (parts.size != 2) {
            output.println(USAGE_GET)
        } else {
            val value = store.get(parts[1]) ?: MSG_KEY_NOT_SET
            output.println(value)
        }
    }

    private fun handleDeleteCommand(parts: List<String>) {
        if (parts.size != 2) {
            output.println(USAGE_DELETE)
        } else {
            store.delete(parts[1])
            output.println("Key deleted.")
        }
    }

    private fun handleCountCommand(parts: List<String>) {
        if (parts.size != 2) {
            output.println(USAGE_COUNT)
        } else {
            val count = store.count(parts[1])
            output.println("Count: $count")
        }
    }

    private companion object {
        // Command usage messages
        const val USAGE_SET = "Usage: SET <key> <value> - Stores the value for the given key."
        const val USAGE_GET = "Usage: GET <key> - Returns the current value for the given key."
        const val USAGE_DELETE = "Usage: DELETE <key> - Removes the entry for the given key."
        const val USAGE_COUNT = "Usage: COUNT <value> - Returns the number of keys that have the given value."
        const val USAGE_COMMIT = "Usage: COMMIT - Completes the current transaction."
        const val USAGE_ROLLBACK = "Usage: ROLLBACK - Reverts to state prior to BEGIN call."
        const val MSG_EXITING_CLI = "Exiting CLI..."
        const val MSG_KEY_NOT_SET = "key not set"
        val MSG_UNKNOWN_COMMAND = """
        Unknown command. Available commands are:
        - SET <key> <value>: Stores the value for the given key.
        - GET <key>: Returns the current value for the given key.
        - DELETE <key>: Removes the entry for the given key.
        - COUNT <value>: Returns the number of keys that have the given value.
        - BEGIN: Starts a new transaction.
        - COMMIT: Completes the current transaction.
        - ROLLBACK: Reverts to state prior to BEGIN call.
        - EXIT: Exits the application.
    """.trimIndent()

        // Commands
        const val CMD_SET = "SET"
        const val CMD_GET = "GET"
        const val CMD_DELETE = "DELETE"
        const val CMD_COUNT = "COUNT"
        const val CMD_BEGIN = "BEGIN"
        const val CMD_COMMIT = "COMMIT"
        const val CMD_ROLLBACK = "ROLLBACK"
        const val CMD_EXIT = "EXIT"
    }

}
