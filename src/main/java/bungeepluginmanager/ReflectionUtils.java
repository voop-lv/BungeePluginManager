package bungeepluginmanager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class ReflectionUtils {

    @SuppressWarnings("unchecked")
    static <T> T getFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        do {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T) field.get(obj);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ignored) {
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        return null;
    }

    static void setFieldValue(Object obj, String fieldName, Object value) {
        Class<?> clazz = obj.getClass();
        do {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ignored) {
            }
        } while ((clazz = clazz.getSuperclass()) != null);
    }

    @SuppressWarnings("unchecked")
    static <T> T getStaticFieldValue(Class<?> clazz, String fieldName) {
        do {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T) field.get(null);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ignored) {
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        return null;
    }

    static void invokeMethod(Object obj, String methodName, Object... args) {
        Class<?> clazz = obj.getClass();
        do {
            try {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals(methodName) && method.getParameterTypes().length == args.length) {
                        method.setAccessible(true);
                        method.invoke(obj, args);
                    }
                }
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
            }
        } while ((clazz = clazz.getSuperclass()) != null);
    }

}
