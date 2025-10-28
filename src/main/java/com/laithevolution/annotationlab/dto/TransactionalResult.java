package com.laithevolution.annotationlab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionalResult {
    private String methodName;
    private String transactionName;
    private boolean activeTransaction;
    private String propagation;
    private Object transactionObject;
    private boolean rolledBack;
    public void markRolledBack() {
        this.rolledBack = true;
    }
}
