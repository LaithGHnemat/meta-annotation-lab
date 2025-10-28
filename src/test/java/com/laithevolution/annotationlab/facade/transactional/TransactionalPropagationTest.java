package com.laithevolution.annotationlab.facade.transactional;

import com.laithevolution.annotationlab.dto.TransactionalScenarioResult;
import com.laithevolution.annotationlab.model.Client;
import com.laithevolution.annotationlab.model.Compliance;
import com.laithevolution.annotationlab.model.Invoice;
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
public class TransactionalPropagationTest {

    @Autowired
    private TransactionalPropagationScenarios propagationScenario;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ComplianceRepository complianceRepository;


    private Client client;
    private Invoice invoice;
    private Compliance compliance;

    @BeforeEach
    void setup() {
        invoiceRepository.deleteAll();
        complianceRepository.deleteAll();
        clientRepository.deleteAll();
        client = Client.builder().name("Laith").email("laith@example.com").build();
        compliance = Compliance.builder().description("Test Compliance").status("NEW").build();
        invoice = Invoice.builder().amount(100.0).status("NEW").build();
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
    void testRequiredParentWithRequiredChildFailure() {
        TransactionalScenarioResult result = propagationScenario
                .parentWithRequiredPropagationInvokingRequiredChildFailure(client, compliance, invoice);

        assertTrue(result.getChild().isRolledBack(), "Child transaction should be rolled back");
        assertTrue(result.getParent().isRolledBack(), "Parent transaction should be rolled back");
        assertEquals(result.getParent().getTransactionName(), result.getChild().getTransactionName(),
                "Parent and Child should share the same transaction");
        assertEquals(1, result.getTotalTransactions(), "Transactions");
        assertEquals(0, clientRepository.count(), "No clients should be persisted");
        assertEquals(0, complianceRepository.count(), "No compliance records should be persisted");
        assertEquals(0, invoiceRepository.count(), "No invoices should be persisted");
    }

    @Test
    void testRequiredParentWithRequiresNewChildFailure() {
        TransactionalScenarioResult result = propagationScenario
                .parentWithRequiredPropagationInvokingRequiresNewChildFailure(client, compliance, invoice);

        assertTrue(result.getChild().isRolledBack(), "Child transaction should be rolled back");
        assertFalse(result.getParent().isRolledBack(), "Parent transaction should NOT be rolled back");
        assertEquals(2, result.getTotalTransactions(), "There should be 2 independent transactions");
        assertEquals(1, clientRepository.count(), "Client should be persisted");
        assertEquals(1, complianceRepository.count(), "Compliance should be persisted");
        assertEquals(0, invoiceRepository.count(), "Invoice should NOT be persisted");

        assertNotSame(result.getParent().getTransactionObject(),
                result.getChild().getTransactionObject(),
                "Parent and Child should have independent transactions");
    }

    @Test
    void testRequiresNewParentWithRequiresNewChildRollback() {
        TransactionalScenarioResult result = propagationScenario
                .parentRequiresNewInvokesChildRollback(client, compliance, invoice);

        assertTrue(result.getChild().isRolledBack(), "Child transaction should be rolled back");

        assertFalse(result.getParent().isRolledBack(), "Parent transaction should NOT be rolled back");
        assertNotSame(result.getParent().getTransactionObject(),
                result.getChild().getTransactionObject(),
                "Parent and Child should have independent transactions");
        assertEquals(1, clientRepository.count(), "Client should be persisted");
        assertEquals(1, complianceRepository.count(), "Compliance should be persisted");
        assertEquals(0, invoiceRepository.count(), "Invoice should NOT be persisted");
    }


    @Test
    void testRequiredParentWithSupportsChildFailure() {
        TransactionalScenarioResult result = propagationScenario
                .parentWithRequiredPropagationInvokingSupportsChildFailure(client, compliance, invoice);
        assertTrue(result.getChild().isRolledBack(), "Child transaction should be rolled back");
        assertTrue(result.getParent().isRolledBack(), "Parent transaction should also be rolled back");

        assertSame(result.getParent().getTransactionObject(),
                result.getChild().getTransactionObject(),
                "Parent and Child should share the same transaction");

        assertEquals(1, result.getTotalTransactions(), "Transactions");

        assertEquals(0, clientRepository.count(), "No clients should be persisted");
        assertEquals(0, complianceRepository.count(), "No compliance records should be persisted");
        assertEquals(0, invoiceRepository.count(), "No invoices should be persisted");
    }

}
