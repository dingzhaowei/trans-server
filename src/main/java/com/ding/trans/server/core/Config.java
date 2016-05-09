package com.ding.trans.server.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Config {

    public static final String PROP_FILE = "trans-server.properties";

    private static Properties props = new Properties();

    static {
        try {
            props.load(getResourceAsStream(PROP_FILE));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read prop file", e);
        }
    }

    public static URL getResource(String resName) {
        return Config.class.getClassLoader().getResource(resName);
    }

    public static InputStream getResourceAsStream(String resName) {
        return Config.class.getClassLoader().getResourceAsStream(resName);
    }

    public static String getValue(String key) {
        return props.getProperty(key);
    }

    public static int getInteger(String key) {
        String value = getValue(key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    public static long getLong(String key) {
        String value = getValue(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    public static double getDouble(String key) {
        String value = getValue(key);
        return value == null ? 0.0 : Double.parseDouble(value);
    }

    public static boolean getBoolean(String key) {
        String value = getValue(key);
        return value == null ? false : Boolean.parseBoolean(value);
    }

}
