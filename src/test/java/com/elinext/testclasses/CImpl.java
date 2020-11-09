package com.elinext.testclasses;

import com.elinext.annotations.Inject;
import com.elinext.testclasses.intercaces.A;
import com.elinext.testclasses.intercaces.B;
import com.elinext.testclasses.intercaces.C;

public class CImpl implements C {
    private final A a;

    @Inject
    public CImpl(A a, B b) {
        this.a = a;
    }

    @Override
    public A getA() {
        return a;
    }
}
