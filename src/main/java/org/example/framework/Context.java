package org.example.framework;

import org.example.annotations.Autowired;
import org.example.annotations.Component;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Context {
    private final Map<String, Class<?>> loadedClasses;

    private Context(Map<String, Class<?>> loadedClasses) {
        this.loadedClasses = loadedClasses;
    }

    public static Context load(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        Map<String, Class<?>> clazzes = reflections.getSubTypesOf(Object.class).stream().filter(clazz -> clazz.isAnnotationPresent(Component.class)).collect(Collectors.toMap(clazz -> clazz.getAnnotation(Component.class).value(), clazz -> clazz));

        return new Context(clazzes);
    }

    public Object get(String className) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        validateClassName(className);
        Class<?> clazz = loadedClasses.get(className);
        Optional<Constructor<?>> annotatedConstructor = getAnnotatedConstructor(className, clazz);

        if (annotatedConstructor.isPresent()) {
            return getAnnotatedConstructor(annotatedConstructor);
        } else {
            return getDefaultConstructor(clazz);
        }
    }

    private Object getDefaultConstructor(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var object = clazz.getConstructor().newInstance();

        var fields = Arrays
                .stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .collect(toList());

        for (Field field : fields) {
            field.setAccessible(true);
            var fieldValue = get(field.getType().getAnnotation(Component.class).value());
            field.set(object, fieldValue);

        }

        return object;
    }

    private Object getAnnotatedConstructor(Optional<Constructor<?>> annotatedConstructor) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        var constructor = annotatedConstructor.get();
        var parameterTypes = constructor.getParameterTypes();
        var params = Arrays.stream(parameterTypes)
                .map(
                        cl -> {
                            try {
                                return get(cl.getAnnotation(Component.class).value());
                            } catch (Exception e) {
                                throw new RuntimeException("Такой тип нельзя подсавлять как параметр");
                            }
                        }

                ).collect(toList());
        return constructor.newInstance(params.toArray());
    }

    private Optional<Constructor<?>> getAnnotatedConstructor(String className, Class<?> clazz) {
        var constructors = clazz.getDeclaredConstructors();
        return Arrays.stream(constructors)
                .filter(con -> con.isAnnotationPresent(Autowired.class))
                .findFirst();
    }

    private void validateClassName(String className) {
        if (!loadedClasses.containsKey(className)) {
            throw new RuntimeException("Нет такого объекта");
        }
    }

}