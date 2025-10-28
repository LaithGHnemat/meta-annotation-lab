package com.laithevolution.annotationlab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionalScenarioResult {
    private TransactionalResult parent;
    private TransactionalResult child;

    public int getTotalTransactions() {
        return Stream.of(parent, child)
                .filter(Objects::nonNull)
                .map(TransactionalResult::getTransactionObject)
                .collect(Collectors.toSet())
                .size();
    }
}
