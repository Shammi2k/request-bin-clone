package com.devtools.requestbin.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging method execution time
 * <p>
 * - Can modify input/output
 * - Can measure execution time
 * - Can handle exceptions
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect
{

  /**
   * Log execution time for all service methods
   */
  @Around("execution(* com.devtools.requestbin.service..*(..))")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint)
    throws Throwable
  {
    long startTime = System.currentTimeMillis();

    String className = joinPoint.getSignature().getDeclaringTypeName();
    String methodName = joinPoint.getSignature().getName();

    try
    {
      Object result = joinPoint.proceed();
      long executionTime = System.currentTimeMillis() - startTime;

      log.debug(
        "{}.{} executed in {} ms",
        className.substring(className.lastIndexOf('.') + 1),
        methodName,
        executionTime);

      return result;
    }
    catch (Exception e)
    {
      long executionTime = System.currentTimeMillis() - startTime;
      log.error(
        "{}.{} failed after {} ms: {}",
        className.substring(className.lastIndexOf('.') + 1),
        methodName,
        executionTime,
        e.getMessage());
      throw e;
    }
  }
}