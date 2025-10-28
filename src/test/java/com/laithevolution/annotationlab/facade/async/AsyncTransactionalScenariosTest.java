package com.laithevolution.annotationlab.facade.async;

import com.laithevolution.annotationlab.model.Client;
import com.laithevolution.annotationlab.reposotory.ClientRepository;
import com.laithevolution.annotationlab.reposotory.ComplianceRepository;
import com.laithevolution.annotationlab.reposotory.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
public class AsyncTransactionalScenariosTest {

    @Autowired
    private AsyncTransactionalScenarios asyncScenarios;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ComplianceRepository complianceRepository;

    private Client client;

    @BeforeEach
    void setup() {
        clientRepository.deleteAll();
        client = Client.builder()
                .name("Laith")
                .email("laith@example.com")
                .build();
    }

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

    @Test
    void testAsyncTransactionalCommit() {
        Client savedClient = clientRepository.saveAndFlush(client);
        String result = asyncScenarios.triggerAsyncTransactionalOperation(savedClient.getId(),
                false);
        assertTrue(result.equalsIgnoreCase("SUCCESS"), "Operation should succeed");
        assertEquals(1, clientRepository.count(), "Client should be persisted");
    }

    @Test
    void testAsyncTransactionalRollback() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            asyncScenarios.triggerAsyncTransactionalOperation(999L, false);});
        assertTrue(exception.getMessage().contains("Async transactional operation failed"));
        assertEquals(0, clientRepository.count(), "No clients should be persisted");
    }
}
