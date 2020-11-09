package com.elinext.exceptions;

public class ConstructorNotFoundException extends RuntimeException {
    public ConstructorNotFoundException(String massage) {
        super(massage);
    }
}
