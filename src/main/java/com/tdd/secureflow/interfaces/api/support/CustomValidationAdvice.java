package com.tdd.secureflow.interfaces.api.support;

import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.domain.support.error.ErrorType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class CustomValidationAdvice {
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void deleteMapping() {
    }

    @Around("postMapping() || putMapping() || deleteMapping()") // joinPoint 의 전후 제어
    public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs(); // JoinPoint의 매개변수
        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult bindingResult = (BindingResult) arg;

                if (bindingResult.hasErrors()) {
                    Map<String, String> errorMap = new HashMap<>();

                    for (FieldError error : bindingResult.getFieldErrors()) {
                        System.out.println("error = " + error);
                        System.out.println("error.getDefaultMessage = " + error.getDefaultMessage());
                        System.out.println("error.getField() = " + error.getField());
                        errorMap.put(error.getField(), error.getDefaultMessage());
                    }

                    throw new CoreException(ErrorType.INVALID_PARAMETER_REQUEST, errorMap);
                }
            }
        }
        return proceedingJoinPoint.proceed();
    }
}

