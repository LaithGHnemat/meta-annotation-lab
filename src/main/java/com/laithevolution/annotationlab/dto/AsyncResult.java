package com.laithevolution.annotationlab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AsyncResult {
    private String methodName;
    private long threadId;
    private String threadName;
    private boolean completed;
    private Long startTimeMillis;
    private Long endTimeMillis;
    private Exception exception;
}
