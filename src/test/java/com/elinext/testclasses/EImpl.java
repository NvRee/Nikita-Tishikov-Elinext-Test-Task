package com.elinext.testclasses;

import com.elinext.annotations.Inject;
import com.elinext.testclasses.intercaces.A;
import com.elinext.testclasses.intercaces.E;

public class EImpl implements E {
    @Inject
    public EImpl() {

    }
    @Inject
    public EImpl(A a) {

    }
}
