package com.spring.tradextransactservice.Config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("within(com.spring.tradextransactservice.Controller..*) || within(com.spring.tradextransactservice.Service..*)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Entering method: {}", methodName);
        long start = System.currentTimeMillis();

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            log.error("Exception in method: {} with message: {}", methodName, t.getMessage());
            throw t;
        }

        long executionTime = System.currentTimeMillis() - start;
        log.info("Exiting method: {} execution time: {} ms", methodName, executionTime);

        return result;
    }
}
