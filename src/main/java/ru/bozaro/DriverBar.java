package ru.bozaro;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Fake java.sql.Driver implementation.
 * This driver register self in static initializer like others.
 * <p>
 * Example of self-registered in static initializer drivers:
 * <ul>
 * <li>org.h2.Driver</li>
 * <li>org.postgresql.Driver</li>
 * </ul>
 * Created by bozaro on 04.09.15.
 */
public class DriverBar implements Driver {
    static {
        register();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return null;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    public static void register() {
        try {
            System.out.println("Register driver: " + DriverBar.class.getName());
            DriverManager.registerDriver(new DriverBar());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
