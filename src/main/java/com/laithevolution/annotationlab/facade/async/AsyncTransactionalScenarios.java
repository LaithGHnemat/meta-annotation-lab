package com.laithevolution.annotationlab.facade.async;

import com.laithevolution.annotationlab.model.Client;
import com.laithevolution.annotationlab.model.Compliance;
import com.laithevolution.annotationlab.model.Invoice;
import com.laithevolution.annotationlab.reposotory.ClientRepository;
import com.laithevolution.annotationlab.reposotory.ComplianceRepository;
import com.laithevolution.annotationlab.reposotory.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncTransactionalScenarios {

    private final ClientRepository clientRepository;

    private final ComplianceRepository complianceRepository;

    private final InvoiceRepository invoiceRepository;

    @Lazy
    @Autowired
    private AsyncTransactionalScenarios self;

    /**
     * Async method with transactional boundary
     * Each async thread gets its own transaction.
     * Rollback or commit inside async does not affect main thread transaction.
     */
    @Async
    @Transactional
    public CompletableFuture<String> asyncTransactionalOperation(Long clientId, boolean fail) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));


        Compliance compliance = Compliance.builder()
                .description("Async Compliance")
                .status("PENDING")
                .client(client)
                .build();
        complianceRepository.save(compliance);

        Invoice invoice = Invoice.builder()
                .amount(1000.0)
                .status("PENDING")
                .compliance(compliance)
                .build();
        invoiceRepository.save(invoice);

        if (fail) {
            throw new RuntimeException("Force rollback in async thread");
        }

        return CompletableFuture.completedFuture("Success");
    }

    public String triggerAsyncTransactionalOperation(Long clientId, boolean fail) {
        try {
            return self.asyncTransactionalOperation(clientId, fail).get();
        } catch (Exception e) {
            throw new RuntimeException("Async transactional operation failed", e);
        }
    }
}
