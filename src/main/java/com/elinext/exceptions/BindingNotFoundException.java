package com.elinext.exceptions;

public class BindingNotFoundException extends RuntimeException {
    public BindingNotFoundException(String massage) {
        super(massage);
    }
}
