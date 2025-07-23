package com.example.customer_management_system.exceptions;

public class DuplicateNicException extends RuntimeException {
    public DuplicateNicException(String message) {
        super(message);
    }
}