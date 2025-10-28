/*
package com.laithevolution.annotationlab.aop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Getter
@Slf4j
public class PropagationMonitorAspect {

    @PersistenceContext
    private EntityManager entityManager;

    private boolean entityManagerOpen;
    private boolean transactionActive;
    private int synchronizationsCount;
    private String lastPropagation;
    private String transactionNameBefore;
    private String transactionNameAfter;
    private int entityManagerBefore;
    private int entityManagerAfter;
    private String nestedTransactionBefore;
    private String nestedTransactionAfter;

    @Around("execution(* com.laithevolution.annotationlab.facade.transactional.fullSuccessScenario.*(..))")
    public Object monitorTransaction(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        Transactional transactional = method.getAnnotation(Transactional.class);
        String propagation = transactional != null ? transactional.propagation().name() : "DEFAULT";
        String isolation = transactional != null ? transactional.isolation().name() : "DEFAULT";
        boolean readOnly = transactional != null && transactional.readOnly();
        this.transactionNameBefore = TransactionSynchronizationManager.getCurrentTransactionName();


        this.entityManagerOpen = entityManager.isOpen();
        this.transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        List<?> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        this.synchronizationsCount = synchronizations.size();
        this.lastPropagation = propagation;

        Object target = pjp.getTarget();
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);

        boolean isCglibProxy = AopUtils.isCglibProxy(target);
        boolean isJdkProxy = AopUtils.isJdkDynamicProxy(target);
        boolean isProxy = AopUtils.isAopProxy(target);

        String proxyType;
        if (isCglibProxy) proxyType = "CGLIB";
        else if (isJdkProxy) proxyType = "JDK Dynamic Proxy";
        else if (isProxy) proxyType = "AOP Proxy (Custom)";
        else proxyType = "Not Proxied (Plain Bean)";

        log.info("────────────────────────────────────────────");
        log.info(" BEFORE [{}] ─ Propagation: {}, Isolation: {}, ReadOnly: {}",
                methodName, propagation, isolation, readOnly);
        log.info("EntityManager open: {}", entityManagerOpen);
        log.info("Transaction active: {}", transactionActive);
        log.info("Transaction name: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("Synchronization active: {}", TransactionSynchronizationManager.isSynchronizationActive());
        log.info("Synchronizations count: {}", synchronizationsCount);
        log.info("Thread: {} (ID={})", Thread.currentThread().getName(), Thread.currentThread().getId());
        log.info("Proxy type: {}", proxyType);
        log.info("Target class: {}", targetClass.getName());
        log.info("Proxy class: {}", target.getClass().getName());
        entityManagerBefore = System.identityHashCode(entityManager);

        if (transactionActive) {
            log.info("Transaction stacktrace (partial, top 5 elements):");
            Arrays.stream(Thread.currentThread().getStackTrace())
                    .filter(ste -> ste.getClassName().startsWith("com.laithevolution"))
                    .limit(5)
                    .forEach(ste -> log.info(" ↳ at {}.{}({}:{})",
                            ste.getClassName(), ste.getMethodName(),
                            ste.getFileName(), ste.getLineNumber()));
        }


        if (target instanceof Advised advised) {
            log.info("Advisors:");
            Arrays.stream(advised.getAdvisors())
                    .forEach(advisor -> log.info(" - {}", advisor.getAdvice().getClass().getName()));
        } else {
            log.info("No advisors found (Not proxied or simple bean)");
        }

        Object result = pjp.proceed();

        entityManagerAfter = System.identityHashCode(entityManager);
        this.transactionNameAfter = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info(" AFTER [{}]", methodName);
        log.info("EntityManager open: {}", entityManager.isOpen());
        log.info("Transaction active: {}", TransactionSynchronizationManager.isActualTransactionActive());
        log.info("Transaction name: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("Synchronization active: {}", TransactionSynchronizationManager.isSynchronizationActive());
        log.info("Synchronizations count: {}", TransactionSynchronizationManager.getSynchronizations().size());

        return result;
    }


    @Around("execution(* com.laithevolution.annotationlab.facade.transactional.nestedRequiredScenario(..))")
    public Object monitorNestedRequiredScenario(ProceedingJoinPoint pjp) throws Throwable {
        nestedTransactionBefore = TransactionSynchronizationManager.getCurrentTransactionName();
        Object result = pjp.proceed();
        nestedTransactionAfter = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("Nested REQUIRED scenario transaction before: {}", nestedTransactionBefore);
        log.info("Nested REQUIRED scenario transaction after: {}", nestedTransactionAfter);
        return result;
    }

}*/
