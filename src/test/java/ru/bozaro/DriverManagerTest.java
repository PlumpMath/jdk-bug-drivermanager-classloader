package ru.bozaro;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Test for reproducing bug with DriverManager and classloader bug.
 */
public class DriverManagerTest {
    @Test
    public void testDriverManager() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // Get drivers.
        Set<Class<? extends Driver>> drivers = getDrivers();
        Assert.assertFalse(drivers.isEmpty());

        WrapperClassLoader loader = new WrapperClassLoader(Thread.currentThread().getContextClassLoader());
        // Load worker in another class loader.
        Class<?> workerClass = loader.loadClass(Worker.class.getName());
        Assert.assertNotSame(Worker.class, workerClass);
        // Run worker.
        Runnable worker = (Runnable) workerClass.getConstructor().newInstance();
        worker.run();
    }

    public static class Worker implements Runnable {
        @Override
        public void run() {
            Set<Class<? extends Driver>> drivers1 = getDrivers();
            Set<Class<? extends Driver>> drivers2 = getDrivers();
            Assert.assertFalse(drivers2.isEmpty());
            Assert.assertFalse(drivers1.isEmpty()); // <!==== FAILURE
        }
    }

    public static Set<Class<? extends Driver>> getDrivers() {
        final Set<Class<? extends Driver>> result = new HashSet<>();
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        System.out.println("DriverManager.getDrivers() - " + DriverManagerTest.class.getClassLoader());
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            result.add(driver.getClass());
            System.out.println(" * " + driver.getClass().getName());
        }
        return result;
    }

    public static class WrapperClassLoader extends ClassLoader {
        public WrapperClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("ru.bozaro.")) {
                try {
                    InputStream resource = getResourceAsStream(name.replace(".", "/") + ".class");
                    if (resource == null) throw new ClassCastException(name);
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] block = new byte[1024];
                    while (true) {
                        int size = resource.read(block);
                        if (size <= 0) break;
                        buffer.write(block, 0, size);
                    }
                    return defineClass(name, buffer.toByteArray(), 0, buffer.size());
                } catch (IOException e) {
                    throw new ClassCastException(name);
                }
            }
            return super.loadClass(name, resolve);
        }
    }
}
