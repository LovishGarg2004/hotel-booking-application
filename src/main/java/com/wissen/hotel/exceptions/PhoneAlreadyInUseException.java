package com.wissen.hotel.exceptions;

public class PhoneAlreadyInUseException extends RuntimeException {
    public PhoneAlreadyInUseException(String message) {
        super(message);
    }
}