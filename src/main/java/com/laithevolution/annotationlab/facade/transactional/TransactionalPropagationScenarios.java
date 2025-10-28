package com.laithevolution.annotationlab.facade.transactional;

import com.laithevolution.annotationlab.dto.TransactionalResult;
import com.laithevolution.annotationlab.dto.TransactionalScenarioResult;
import com.laithevolution.annotationlab.model.Client;
import com.laithevolution.annotationlab.model.Compliance;
import com.laithevolution.annotationlab.model.Invoice;
import com.laithevolution.annotationlab.service.ClientService;
import com.laithevolution.annotationlab.service.ComplianceService;
import com.laithevolution.annotationlab.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class TransactionalPropagationScenarios {

    private final ClientService clientService;
    private final InvoiceService invoiceService;
    private final ComplianceService complianceService;

    @Autowired
    @Lazy
    private TransactionalPropagationScenarios self;

    /**
     * Scenario 1: Parent with REQUIRED propagation invoking a Child with REQUIRED propagation.
     * Both share the same transaction. An exception in the child rolls back the entire transaction.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TransactionalScenarioResult parentWithRequiredPropagationInvokingRequiredChildFailure(
            Client client, Compliance compliance, Invoice invoice) {

        Object parentTx = TransactionAspectSupport.currentTransactionStatus();

        TransactionalResult parentResult = TransactionalResult.builder()
                .methodName("parentWithRequiredPropagationInvokingRequiredChildFailure")
                .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                .transactionObject(parentTx)
                .propagation("REQUIRED")
                .rolledBack(false)
                .build();

        clientService.createClient(client);
        compliance.setClient(client);
        complianceService.createCompliance(compliance);

        TransactionalResult childResult;
        try {
            childResult = childWithRequiredPropagation(client, compliance, invoice);
        } catch (RuntimeException e) {


            childResult = TransactionalResult.builder()
                    .methodName("childWithRequiredPropagation")
                    .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                    .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                    .propagation("REQUIRED")
                    .rolledBack(true)
                    .build();

        }

        if (childResult.isRolledBack())
            parentResult.markRolledBack();


        return new TransactionalScenarioResult(parentResult, childResult);
    }

    /**
     * Child method with REQUIRED propagation.
     * Throws an exception to force rollback of the shared transaction.
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public TransactionalResult childWithRequiredPropagation(
            Client client, Compliance compliance, Invoice invoice) {
        Object childTx = TransactionAspectSupport.currentTransactionStatus();

        TransactionalResult childResult;
        try {
            childResult = TransactionalResult.builder()
                    .methodName("childWithRequiredPropagation")
                    .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                    .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                    .propagation("REQUIRED")
                    .rolledBack(false)
                    .build();

            invoice.setCompliance(compliance);
            invoiceService.createInvoice(invoice);
            throw new RuntimeException("Child REQUIRED forced rollback");

        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            childResult = TransactionalResult.builder()
                    .methodName("childWithRequiredPropagation")
                    .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                    .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                    .propagation("REQUIRED")
                    .transactionObject(childTx)
                    .rolledBack(true)
                    .build();

        }
        return childResult;
    }

    /**
     * Scenario 2: Parent with REQUIRED propagation invoking a Child with REQUIRES_NEW propagation.
     * The child runs in an independent transaction. An exception in the child rolls back only the child's transaction,
     * while the parent's transaction continues and commits successfully.
     */


    @Transactional(propagation = Propagation.REQUIRED)
    public TransactionalScenarioResult parentWithRequiredPropagationInvokingRequiresNewChildFailure(
            Client client, Compliance compliance, Invoice invoice) {
        Object parentTx = TransactionAspectSupport.currentTransactionStatus();

        // Parent transaction result
        TransactionalResult parentResult = TransactionalResult.builder()
                .methodName("parentWithRequiredPropagationInvokingRequiresNewChildFailure")
                .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                .propagation("REQUIRED")
                .transactionObject(parentTx)
                .rolledBack(false)
                .build();

        clientService.createClient(client);
        compliance.setClient(client);
        complianceService.createCompliance(compliance);
        TransactionalResult childResult = self.childWithRequiresNewPropagation(client, compliance, invoice);
        return new TransactionalScenarioResult(parentResult, childResult);
    }

    /**
     * Child method with REQUIRES_NEW propagation.
     * Runs in a new, independent transaction. An exception forces rollback only for this child transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionalResult childWithRequiresNewPropagation(
            Client client, Compliance compliance, Invoice invoice) {
        Object childTx = TransactionAspectSupport.currentTransactionStatus();

        TransactionalResult childResult;
        try {
            invoice.setCompliance(compliance);
            invoiceService.createInvoice(invoice);


            throw new RuntimeException("Child REQUIRES_NEW forced rollback");

        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            childResult = TransactionalResult.builder()
                    .methodName("childWithRequiresNewPropagation")
                    .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                    .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                    .propagation("REQUIRES_NEW")
                    .transactionObject(childTx)
                    .rolledBack(true)
                    .build();
        }

        return childResult;
    }

    /**
     * Scenario 3: Parent with REQUIRES_NEW propagation invoking a Child with REQUIRES_NEW propagation.
     * Both transactions are independent. An exception in the child rolls back only the child's transaction,
     * while the parent's transaction continues and commits successfully.
     */


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionalScenarioResult parentRequiresNewInvokesChildRollback(
            Client client, Compliance compliance, Invoice invoice) {

        Object parentTx = TransactionAspectSupport.currentTransactionStatus();

        TransactionalResult parentResult = TransactionalResult.builder()
                .methodName("parentRequiresNewInvokesChildRollback")
                .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                .transactionObject(parentTx)
                .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                .propagation("REQUIRES_NEW")
                .rolledBack(false)
                .build();

        clientService.createClient(client);
        compliance.setClient(client);
        complianceService.createCompliance(compliance);

        TransactionalResult childResult = self.childRequiresNewRollbackOnly(client, compliance, invoice);

        return new TransactionalScenarioResult(parentResult, childResult);
    }

    /**
     * Child method with REQUIRES_NEW propagation.
     * Runs in an independent transaction. Exception forces rollback only for this child transaction.
     */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionalResult childRequiresNewRollbackOnly(
            Client client, Compliance compliance, Invoice invoice) {

        Object childTx = TransactionAspectSupport.currentTransactionStatus();

        TransactionalResult childResult;
        try {
            invoice.setCompliance(compliance);
            invoiceService.createInvoice(invoice);
            throw new RuntimeException("Child REQUIRES_NEW forced rollback");

        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            childResult = TransactionalResult.builder()
                    .methodName("childRequiresNewRollbackOnly")
                    .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                    .transactionObject(childTx)
                    .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                    .propagation("REQUIRES_NEW")
                    .rolledBack(true)
                    .build();
        }

        return childResult;
    }


    /**
     * Scenario 4: Parent with REQUIRED propagation invoking a Child with SUPPORTS propagation.
     * Child runs within parent's transaction. Exception in child marks transaction rollback-only,
     * parent will be rolled back when it tries to commit.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TransactionalScenarioResult parentWithRequiredPropagationInvokingSupportsChildFailure(
            Client client, Compliance compliance, Invoice invoice) {

        Object parentTx = TransactionAspectSupport.currentTransactionStatus();

        TransactionalResult parentResult = TransactionalResult.builder()
                .methodName("parentWithRequiredPropagationInvokingSupportsChildFailure")
                .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                .transactionObject(parentTx)
                .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                .propagation("REQUIRED")
                .rolledBack(false)
                .build();

        clientService.createClient(client);
        compliance.setClient(client);
        complianceService.createCompliance(compliance);

        TransactionalResult childResult;
        try {
            childResult = childWithSupportsPropagation(client, compliance, invoice);
        } catch (RuntimeException e) {
            childResult = TransactionalResult.builder()
                    .methodName("childWithSupportsPropagation")
                    .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                    .transactionObject(parentTx)
                    .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                    .propagation("SUPPORTS")
                    .rolledBack(true)
                    .build();
        }

        // Update parentResult rollback flag إذا child رمى Exception
        if (childResult.isRolledBack()) {
            parentResult.markRolledBack();
        }

        return new TransactionalScenarioResult(parentResult, childResult);
    }

    /**
     * Child method with SUPPORTS propagation.
     * Joins existing transaction. Exception marks rollback-only.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public TransactionalResult childWithSupportsPropagation(
            Client client, Compliance compliance, Invoice invoice) {

        Object childTx = TransactionAspectSupport.currentTransactionStatus();
        TransactionalResult childResult;
        try {
            invoice.setCompliance(compliance);
            invoiceService.createInvoice(invoice);

            // Simulate failure
            throw new RuntimeException("Child SUPPORTS forced rollback");

        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            childResult = TransactionalResult.builder()
                    .methodName("childWithSupportsPropagation")
                    .transactionName(TransactionSynchronizationManager.getCurrentTransactionName())
                    .transactionObject(childTx)
                    .activeTransaction(TransactionSynchronizationManager.isActualTransactionActive())
                    .propagation("SUPPORTS")
                    .rolledBack(true)
                    .build();
        }

        return childResult;
    }
}