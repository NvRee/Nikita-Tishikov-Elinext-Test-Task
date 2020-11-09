package com.elinext;

import com.elinext.annotations.Inject;
import com.elinext.exceptions.BindingNotFoundException;
import com.elinext.exceptions.ConstructorNotFoundException;
import com.elinext.exceptions.TooManyConstructorsException;
import com.elinext.interfaces.Injector;
import com.elinext.interfaces.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InjectorImpl implements Injector {

    //Bean storage location
    private final Map<Class<?>, Bean<?>> beans = new ConcurrentHashMap<>();

    @Override
    public synchronized <T> Provider<T> getProvider(Class<T> type) {
        if (!beans.containsKey(type)) {
            return null;
        }
        Bean<T> bean = (Bean<T>) beans.get(type);
        T result = bean.getInstance();
        return new ProviderImpl<>(result);
    }

    @Override
    public synchronized <T> void bind(Class<T> intf, Class<? extends T> impl) {
        createBean(intf, impl, BeanPrototype::new);
    }

    @Override
    public synchronized <T> void bindSingleton(Class<T> intf, Class<? extends T> impl) {
        createBean(intf, impl, BeanSingleton::new);
    }

    //Create bean
    private <T> void createBean(Class<T> intf, Class<? extends T> impl, function<Bean<?>, T> factory) {
        //Checking if such a bean has been added before
        if (beans.containsKey(intf)) {
            throw new IllegalArgumentException(String.format("The %s implementation is already installed", intf));
        }
        //Finding and testing the desired constructor
        List<Constructor<?>> constructors = Arrays.stream(impl.getConstructors()).filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .collect(Collectors.toList());
        if (constructors.size() > 1) {
            throw new TooManyConstructorsException("There should be no more than one constructor marked with Inject annotation");
        } else if (constructors.size() == 0) {
            try {
                Constructor<?> constructor = impl.getConstructor();
                if (!Modifier.isPublic(constructor.getModifiers())) {
                    throw new ConstructorNotFoundException(String.format("No valid constructor found in %s", impl));
                }
                constructors.add(constructor);
            } catch (NoSuchMethodException e) {
                throw new ConstructorNotFoundException(String.format("No valid constructor found in %s", impl));
            }
        }

        //Checking constructor parameters
        List<Class<?>> params = Arrays.stream(constructors.get(0).getParameterTypes()).filter(key -> !beans.containsKey(key)).collect(Collectors.toList());
        if (params.size() != 0) {
            StringBuilder stringBuilder = new StringBuilder("The container cannot find binding with names: ");
            stringBuilder.append(params.get(0).getName());
            for (int i = 1; i < params.size(); i++) {
                stringBuilder.append(", ");
                stringBuilder.append(params.get(i));
            }
            throw new BindingNotFoundException(stringBuilder.toString());
        }

        //Adding bean to Map
        beans.put(intf, factory.apply(intf, impl, constructors.get(0)));
    }

    //Creating a return object
    //Uses recursion
    private Object createObject(final Constructor<?> constructor) {
        //Search and creation of the necessary parameters
        Object[] parameters = Arrays.stream(constructor.getParameterTypes()).map(parameter -> {
            Bean<?> bean = beans.get(parameter);
            return bean.getInstance();
        }).toArray();

        //Creating the desired object
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e.getMessage()); //This shouldn't happen!
        }
    }

    //Interface for code shortening
    @FunctionalInterface
    private interface function<R, T> {
        R apply(Class<T> intf, Class<? extends T> impl, Constructor<?> constructor);
    }

    private interface Bean<T> {
        T getInstance();
    }

    //Bean implementation
    private static abstract class BeanAbstractImpl<T> implements Bean<T> {
        protected final Class<T> intf;
        protected final Class<? extends T> impl;
        protected final Constructor<?> constructor;

        private BeanAbstractImpl(Class<T> intf, Class<? extends T> impl, Constructor<?> constructor) {
            this.intf = intf;
            this.impl = impl;
            this.constructor = constructor;
        }
    }

    //Bean singleton implementation
    private class BeanSingleton<T> extends BeanAbstractImpl<T> {

        private T object;

        private BeanSingleton(Class<T> intf, Class<? extends T> impl, Constructor<?> constructor) {
            super(intf, impl, constructor);
        }

        @Override
        public T getInstance() {
            if (object != null) {
                return object;
            }
            object = (T) createObject(constructor);
            return object;
        }
    }

    //Bean prototype implementation
    private class BeanPrototype<T> extends BeanAbstractImpl<T> {

        private BeanPrototype(Class<T> intf, Class<? extends T> impl, Constructor<?> constructor) {
            super(intf, impl, constructor);
        }

        @Override
        public T getInstance() {
            return (T) createObject(constructor);
        }
    }
}