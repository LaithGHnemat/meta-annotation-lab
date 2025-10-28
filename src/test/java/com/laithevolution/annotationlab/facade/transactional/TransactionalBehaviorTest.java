package com.laithevolution.annotationlab.facade.transactional;

import com.laithevolution.annotationlab.exceptions.CustomCheckedException;
import com.laithevolution.annotationlab.exceptions.IgnoredRuntimeException;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
public class TransactionalBehaviorTest {

    @Autowired
    private TransactionalBehaviorScenarios scenarios;

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
                .name("Laith")
                .email("laith@example.com")
                .build();
        clientRepository.save(client);
    }

    @Test
    void testReadOnlyTransaction() {
        List<Client> clients = scenarios.findAllClientsReadOnly();
        assertFalse(clients.isEmpty(), "Should return clients without modifying database");
    }

    @Test
    void testTimeoutTransaction() {
        assertThrows(Exception.class, () -> scenarios.simulateTimeoutTransaction(),
                "Should throw exception due to timeout");
    }

    @Test
    void testRollbackForCheckedException() {
        clientRepository.deleteAll();
        assertThrows(CustomCheckedException.class, () ->
                scenarios.saveClientWithCheckedException(
                        Client.builder().name("RollbackTest").email("test@example.com").build()));
        assertEquals(0, clientRepository.count(),
                "Client should not be persisted due to rollback");
    }

    @Test
    void testNoRollbackForRuntimeException() {
        clientRepository.deleteAll();
        Client newClient = Client.builder().name("NoRollbackTest").email("nrb@example.com").build();
        assertThrows(IgnoredRuntimeException.class, () ->
                scenarios.saveClientWithNoRollback(newClient));
        assertEquals(1, clientRepository.count(),
                "Client should be persisted despite runtime exception due to noRollbackFor");
    }
}
