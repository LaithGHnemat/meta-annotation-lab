package com.laithevolution.annotationlab.facade.transactional;

import com.laithevolution.annotationlab.exceptions.CustomCheckedException;
import com.laithevolution.annotationlab.exceptions.IgnoredRuntimeException;
import com.laithevolution.annotationlab.model.Client;
import com.laithevolution.annotationlab.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionalBehaviorScenarios {

    private final ClientService clientService;

    /**
     * Scenario 1: READ_ONLY
     * Demonstrates read-only transaction
     */
    @Transactional(readOnly = true)
    public List<Client> findAllClientsReadOnly() {
        return clientService.findAll();
    }

    /**
     * Scenario 2: TIMEOUT
     * Demonstrates transaction timeout
     */
    @Transactional(timeout = 2)
    public void simulateTimeoutTransaction() {
        for (int i = 0; i < 100_000; i++) {
            Client client = Client.builder()
                    .name("TimeoutTest-" + i)
                    .email("timeout" + i + "@example.com")
                    .build();
            clientService.createClient(client);
        }
    }

    /**
     * Scenario 3: ROLLBACK_FOR
     * Forces rollback for checked exception
     */
    @Transactional(rollbackFor = CustomCheckedException.class)
    public void saveClientWithCheckedException(Client client) throws CustomCheckedException {
        clientService.createClient(client);
        throw new CustomCheckedException("Force rollback");
    }

    /**
     * Scenario 4: NO_ROLLBACK_FOR
     * Ignore rollback for specific exception
     */
    @Transactional(noRollbackFor = {IgnoredRuntimeException.class})
    public void saveClientWithNoRollback(Client client) {
        clientService.createClient(client);
        throw new IgnoredRuntimeException("Should NOT rollback");
    }

}
