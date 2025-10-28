package com.laithevolution.annotationlab.facade.transactional;

import com.laithevolution.annotationlab.model.Client;
import com.laithevolution.annotationlab.reposotory.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
public class TransactionalIsolationTest {

    @Autowired
    private TransactionalIsolationScenarios isolationScenarios;

    @Autowired
    private ClientRepository clientRepository;

    private Client client;

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:15.3")
                    .withDatabaseName("testdb")
                    .withUsername("sa")
                    .withPassword("sa");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @BeforeEach
    void setup() {
        clientRepository.deleteAll();
        client = Client.builder()
                .name("OriginalName")
                .email("laith@example.com")
                .build();
        clientRepository.save(client);
    }

    /**
     *  Test Goal:
     * Verify that READ_COMMITTED isolation level prevents dirty reads.
     * The reader should NOT see uncommitted updates made by another transaction.
     */
    @Test
    void testReadCommittedPreventsDirtyRead() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Task 1: Writer updates the client name but does NOT commit immediately.
        Future<?> writer = executor.submit(() -> {
            isolationScenarios.simulateWriterUncommittedUpdate(client.getId());
        });

        // Task 2: Reader tries to read the client while the writer's transaction is still uncommitted.
        Future<String> readerDuringUncommitted = executor.submit(() -> {
            Thread.sleep(1000); // Wait briefly to ensure the writer updates before the read occurs.
            return isolationScenarios.readClientNameDuringUncommittedUpdate(client.getId());
        });

        String valueSeenByReader = readerDuringUncommitted.get(); // The value observed by the reader.
        writer.get(); // Wait for the writer to complete.

        // After the writer commits, read the client name again.
        String valueAfterCommit = isolationScenarios.readClientNameAfterCommit(client.getId());

        //  Assertions
        assertEquals("OriginalName", valueSeenByReader,
                "Reader should NOT see uncommitted update (READ_COMMITTED is working correctly)");
        assertEquals("UpdatedName-Uncommitted", valueAfterCommit,
                "Reader should see the updated name after the writer transaction commits");
    }


    /**
     *  Goal:
     * Verify that REPEATABLE_READ prevents non-repeatable reads.
     * The same transaction should always see consistent data, even if another transaction updates it concurrently.
     */

    @Test
    void testRepeatableReadPreventsNonRepeatableRead() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Reader thread: reads the same client twice in one transaction
        Future<String> readerResult = executor.submit(() ->
                isolationScenarios.readClientTwiceWithinSameTransaction(client.getId())
        );

        //  Writer thread: updates client name after 1 second (simulating concurrent modification)
        Future<?> writer = executor.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Client updated = clientRepository.findById(client.getId()).orElseThrow();
            updated.setName("ModifiedByAnotherTx");
            clientRepository.save(updated);
        });

        // Wait for both threads
        String readerOutcome = readerResult.get();
        writer.get();

        //  Assertions
        assertEquals("Consistent reads (no non-repeatable read)", readerOutcome,
                "Under REPEATABLE_READ, the same transaction " +
                        "should see consistent data even if another transaction updates the record.");
    }


    @Test
    void testSerializableIsolationPreventsConcurrentModification() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Transaction A: Writer updates the client
        Future<?> txA = executor.submit(() -> {
            isolationScenarios.simulateSerializableReadWrite(client.getId());
        });

        // Transaction B: Concurrent Writer tries to update same client
        Future<Boolean> txB = executor.submit(() -> {
            try {
                Thread.sleep(500); // Give A a head start
                isolationScenarios.simulateSerializableReadWrite(client.getId());
                return true; // update succeeded
            } catch (Exception e) {
                // Expected conflict due to SERIALIZABLE isolation
                return false;
            }
        });

        // Wait for both transactions
        txA.get(6, TimeUnit.SECONDS);
        boolean txBsuccess = txB.get(6, TimeUnit.SECONDS);

        //  Assertion: Under SERIALIZABLE, one of the concurrent writers should fail
        assertFalse(txBsuccess, "At least one transaction should fail under SERIALIZABLE isolation due to concurrency conflict");

        //  Final check: database reflects first committed transaction
        String finalName = isolationScenarios.readClientNameAfterCommit(client.getId());
        assertTrue(finalName.contains("SerializableUpdate"), "After commit, the client should have the updated value from the first transaction");
    }

}
