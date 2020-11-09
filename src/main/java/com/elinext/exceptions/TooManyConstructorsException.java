package com.elinext.exceptions;

public class TooManyConstructorsException extends RuntimeException {
    public TooManyConstructorsException(String massage) {
        super(massage);
    }
}
