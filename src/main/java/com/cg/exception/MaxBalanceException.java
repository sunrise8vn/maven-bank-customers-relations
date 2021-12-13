package com.cg.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MaxBalanceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MaxBalanceException(String message) {
        super(message);
    }
}
