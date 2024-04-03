# Transactional Key-Value Store

Main classes
are [ConcurrentTransactionalKeyValueStore](src/main/kotlin/com/antonshillov/transactionalkv/store/ConcurrentTransactionalKeyValueStore.kt)
and [SynchronousTransactionalKeyValueStore](src/main/kotlin/com/antonshillov/transactionalkv/store/SynchronousTransactionalKeyValueStore.kt).

SynchronousTransactionalKeyValueStore is only for illustration purposes to show how it fails in multithreading case

## Run

To run launch [Main.kt](src/main/kotlin/com/antonshillov/transactionalkv/Main.kt) `main` function

## Assumptions

While working in concurrent environment I assumed that transaction consistency was the most important factor.
To correctly handle it I've chosen _Serializable isolation level_.
If one thread transaction is active, all other operations will be locked until it completes by COMMIT or ROLLBACK

## Potential optimisations

1. Track operations instead of whole map copy for transactions to reduce memory usage. Will complicate get and count
   implementations a bit
2. Cache COUNT operations in LRU cache to have O(1) complexity for frequent counts
3. Use more relaxed transaction isolation level to improve multi-thread performance
