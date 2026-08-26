package com.codems.ordertracker.common.audit.aspect;

import com.codems.ordertracker.common.exceptions.types.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class ApplicationLoggingAspect {

    @Pointcut("within(com.codems.ordertracker..*) && @within(org.springframework.web.bind.annotation.RestController)")
    void controllerMethods() {
    }

    @Pointcut("within(com.codems.ordertracker..*) && @within(org.springframework.stereotype.Service)")
    void serviceMethods() {
    }

    @Around("controllerMethods() || serviceMethods()")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        String method = methodName(joinPoint);
        log.debug("Started {}", method);

        Object result = joinPoint.proceed();

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        log.info("Completed {} in {} ms", method, durationMs);
        return result;
    }

    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String method = methodName(joinPoint);
        if (exception instanceof BaseException baseException) {
            log.warn("Failed {} with code {}: {}", method, baseException.getCode(), baseException.getMessage());
            return;
        }
        log.error("Failed {} with {}", method, exception.getClass().getSimpleName(), exception);
    }

    private String methodName(JoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "."
                + joinPoint.getSignature().getName();
    }
}
