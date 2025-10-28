package com.laithevolution.annotationlab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AsyncExecutionResult {

    private String methodName;
    private long mainThreadId;
    private long mainThreadStartTimeMillis;
    private long mainThreadEndTimeMillis;
    private long asyncThreadId;
    private String asyncThreadName;
    private long asyncThreadStartTimeMillis;
    private long asyncThreadEndTimeMillis;
    private boolean asyncCompleted;
    private Exception exception;

}

