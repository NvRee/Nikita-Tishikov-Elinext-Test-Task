package com.elinext;

import com.elinext.exceptions.BindingNotFoundException;
import com.elinext.exceptions.ConstructorNotFoundException;
import com.elinext.exceptions.TooManyConstructorsException;
import com.elinext.testclasses.*;
import com.elinext.testclasses.intercaces.*;
import com.elinext.interfaces.Injector;
import com.elinext.interfaces.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class injectorTest {

    private Injector injector;

    @BeforeEach
    public void setUp() {
        injector = new InjectorImpl();
    }

    @Test
    void testExistingBinding() {
        injector.bind(A.class, AImpl.class);
        Provider<A> daoProvider = injector.getProvider(A.class);
        assertNotNull(daoProvider);
        assertNotNull(daoProvider.getInstance());
        assertSame(AImpl.class, daoProvider.getInstance().getClass());
    }

    @Test
    void testPrototype() {
        injector.bind(A.class, AImpl.class);
        A a1 = injector.getProvider(A.class).getInstance();
        A a2 = injector.getProvider(A.class).getInstance();
        assertNotEquals(a1, a2);
    }

    @Test
    void testSingleton() {
        injector.bindSingleton(A.class, AImpl.class);
        A a1 = injector.getProvider(A.class).getInstance();
        A a2 = injector.getProvider(A.class).getInstance();
        assertEquals(a1, a2);
    }

    @Test
    void testTooManyConstructorsException() {
        TooManyConstructorsException exception = assertThrows(TooManyConstructorsException.class,
                () -> injector.bind(E.class, EImpl.class));
        assertEquals(exception.getMessage(),
                "There should be no more than one constructor marked with Inject annotation");
    }

    @Test
    void testConstructorNotFoundException() {
        ConstructorNotFoundException exception = assertThrows(ConstructorNotFoundException.class,
                () -> injector.bind(F.class, FImpl.class));
        assertEquals(exception.getMessage(), "No valid constructor found in class com.elinext.testclasses.FImpl");
    }

    @Test
    void testPrivateConstructor() {
        ConstructorNotFoundException exception = assertThrows(ConstructorNotFoundException.class,
                () -> injector.bind(D.class, DImpl.class));
        assertEquals(exception.getMessage(), "No valid constructor found in class com.elinext.testclasses.DImpl");
    }

    @Test
    void testBindingNotFoundException() {
        BindingNotFoundException exception = assertThrows(BindingNotFoundException.class,
                () -> injector.bind(B.class, BImpl.class));
        assertEquals(exception.getMessage(), "The container cannot find binding with names: com.elinext.testclasses.intercaces.A");
    }

    @Test
    void testNullProvider() {
        assertNull(injector.getProvider(A.class));
    }

    @Test
    void testInheritanceTree() {
        injector.bindSingleton(A.class, AImpl.class);
        injector.bindSingleton(B.class, BImpl.class);
        injector.bindSingleton(C.class, CImpl.class);
        assertEquals(injector.getProvider(B.class).getInstance().getA(),
                injector.getProvider(C.class).getInstance().getA());
    }

}
