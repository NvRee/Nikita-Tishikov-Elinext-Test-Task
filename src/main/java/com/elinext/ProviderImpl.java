package com.elinext;

import com.elinext.interfaces.Provider;

public class ProviderImpl<T> implements Provider<T> {

    private final T instance;

    public ProviderImpl(T instance) {
        this.instance = instance;
    }

    @Override
    public T getInstance() {
        return instance;
    }
}
