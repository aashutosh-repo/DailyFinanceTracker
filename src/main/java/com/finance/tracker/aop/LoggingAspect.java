package com.finance.tracker.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final long SLOW_METHOD_THRESHOLD_MS = 1000;

    /**
     * Logs service method execution.
     *
     * Logs:
     * - Method start
     * - Successful completion + execution time
     * - Failed execution + execution time
     * - Slow method warning
     *
     * Arguments and return values are intentionally not logged
     * to avoid exposing sensitive financial/user data.
     */
    @Around("execution(* com.finance.tracker.service..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        String className = joinPoint
                .getTarget()
                .getClass()
                .getSimpleName();

        String methodName = joinPoint
                .getSignature()
                .getName();

        String method = className + "." + methodName + "()";

        long startTime = System.currentTimeMillis();

        log.debug("START {}", method);

        try {

            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            if (duration >= SLOW_METHOD_THRESHOLD_MS) {
                log.warn("SLOW METHOD {} duration={}ms", method, duration);
            } else {
                log.debug("END {} duration={}ms", method, duration);
            }

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("FAILED {} duration={}ms exception={} message={}", method, duration, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}
