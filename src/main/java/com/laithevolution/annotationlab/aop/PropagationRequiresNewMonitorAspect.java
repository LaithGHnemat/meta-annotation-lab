/*
package com.laithevolution.annotationlab.aop;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
@Getter
@Slf4j
public class PropagationRequiresNewMonitorAspect {
    private String requiredParentBefore;
    private String requiredParentAfter;
    private String requiredChildBefore;
    private String requiredChildAfter;

    private String requiresNewParentBefore;
    private String requiresNewParentAfter;
    private String requiresNewChildBefore;
    private String requiresNewChildAfter;

    // ----------------- REQUIRED parent + REQUIRED child -----------------

    @Around("execution(* com.laithevolution.annotationlab.facade.transactional.PropagationRequiresNew.requiredParentWithChildFailure(..))")
    public Object monitorRequiredParent(ProceedingJoinPoint pjp) throws Throwable {
        requiredParentBefore = TransactionSynchronizationManager.getCurrentTransactionName();
        Object result;
        try {
            result = pjp.proceed();
        } finally {
            requiredParentAfter = TransactionSynchronizationManager.getCurrentTransactionName();
            log.info("Required Parent transaction after: {}", requiredParentAfter);
        }
        return result;
    }

    @Around("execution(* com.laithevolution.annotationlab.facade.transactional.PropagationRequiresNew.requiredChild(..))")
    public Object monitorRequiredChild(ProceedingJoinPoint pjp) throws Throwable {
        requiredChildBefore = TransactionSynchronizationManager.getCurrentTransactionName();
        Object result;
        try {
            result = pjp.proceed();
        } finally {
            requiredChildAfter = TransactionSynchronizationManager.getCurrentTransactionName();
            log.info("REQUIRED child transaction before: {}", requiredChildBefore);
            log.info("REQUIRED child transaction after: {}", requiredChildAfter);
        }
        return result;
    }

    // ----------------- REQUIRED parent + REQUIRES_NEW child -----------------

    @Around("execution(* com.laithevolution.annotationlab.facade.transactional.PropagationRequiresNew.requiredParentWithRequiresNewChildFailure(..))")
    public Object monitorRequiresNewParent(ProceedingJoinPoint pjp) throws Throwable {
        requiresNewParentBefore = TransactionSynchronizationManager.getCurrentTransactionName();
        Object result;
        try {
            result = pjp.proceed();
        } finally {
            requiresNewParentAfter = TransactionSynchronizationManager.getCurrentTransactionName();
            log.info("REQUIRED parent (with REQUIRES_NEW child) before: {}", requiresNewParentBefore);
            log.info("REQUIRED parent (with REQUIRES_NEW child) after: {}", requiresNewParentAfter);
        }
        return result;
    }

    @Around("execution(* com.laithevolution.annotationlab.facade.transactional.PropagationRequiresNew.requiresNewChild(..))")
    public Object monitorRequiresNewChild(ProceedingJoinPoint pjp) throws Throwable {
        requiresNewChildBefore = TransactionSynchronizationManager.getCurrentTransactionName();
        Object result;
        try {
            result = pjp.proceed();
        } finally {
            requiresNewChildAfter = TransactionSynchronizationManager.getCurrentTransactionName();
            log.info("REQUIRES_NEW child transaction before: {}", requiresNewChildBefore);
            log.info("REQUIRES_NEW child transaction after: {}", requiresNewChildAfter);
        }
        return result;
    }
}
*/
