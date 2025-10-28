package com.laithevolution.annotationlab.facade.transactional;

import com.laithevolution.annotationlab.model.Client;
import com.laithevolution.annotationlab.service.ClientService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionalIsolationScenarios {

    private final ClientService clientService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Scenario 1: READ_COMMITTED
     * Demonstrates prevention of dirty reads.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void simulateWriterUncommittedUpdate(Long clientId) {
        Client client = clientService.findById(clientId);
        client.setName("UpdatedName-Uncommitted");
        clientService.updateClient(client);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public String readClientNameDuringUncommittedUpdate(Long clientId) {
        Client client = clientService.findById(clientId);
        return client.getName();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public String readClientNameAfterCommit(Long clientId) {
        entityManager.clear();
        Client client = clientService.findById(clientId);
        return client.getName();
    }


    /**
     * Scenario 2: REPEATABLE_READ
     * Demonstrates prevention of non-repeatable reads.
     * The reader should see the same data within the same transaction, even if another transaction updates it.
     */

    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public String readClientTwiceWithinSameTransaction(Long clientId) {
        Client firstRead = clientService.findById(clientId);

        // Simulate external update happening in another thread
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Client secondRead = clientService.findById(clientId);

        if(Objects.isNull(secondRead))
            return "Inconsistent reads (non-repeatable read occurred)";

        return firstRead.getName().equals(secondRead.getName())
                ? "Consistent reads (no non-repeatable read)"
                : "Inconsistent reads (non-repeatable read occurred)";
    }

    /**
     * Scenario 3: SERIALIZABLE
     * Demonstrates the strictest isolation level — transactions execute as if they were serialized.
     * Prevents phantom reads and ensures complete data consistency.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void simulateSerializableReadWrite(Long clientId) {
        Client client = clientService.findById(clientId);
        client.setName(client.getName() + "-SerializableUpdate");
        clientService.updateClient(client);

        // Hold the transaction open to simulate concurrency properly
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Scenario 4: PHANTOM READ prevention
     * Demonstrates how SERIALIZABLE isolation prevents phantom reads.
     * Reader executes a query twice while a concurrent writer inserts a new record.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public boolean detectPhantomReadDuringConcurrentInsert(String clientName) {
        // First read: fetch all clients with a specific name pattern
        List<Client> firstRead = clientService.findByNameLike(clientName);

        // Simulate a concurrent transaction inserting a new matching client
        try {
            Thread.sleep(2000); // allow writer to insert
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Second read: fetch same query again
        List<Client> secondRead = clientService.findByNameLike(clientName);

        // Return true if phantom read occurred
        return firstRead.size() != secondRead.size();
    }

    /**
     * Helper writer method for scenario 4
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void insertClientDuringReader(String name, String email) {
        Client newClient = Client.builder()
                .name(name)
                .email(email)
                .build();
        clientService.createClient(newClient);

        try {
            Thread.sleep(1000); // keep transaction open for reader to run
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
