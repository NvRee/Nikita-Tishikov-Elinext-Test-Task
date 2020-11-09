package com.elinext.testclasses;

import com.elinext.annotations.Inject;
import com.elinext.testclasses.intercaces.A;
import com.elinext.testclasses.intercaces.B;

public class BImpl implements B {
    private final A a;

    @Inject
    public BImpl(A a) {
        this.a = a;
    }

    @Override
    public A getA() {
        return a;
    }
}