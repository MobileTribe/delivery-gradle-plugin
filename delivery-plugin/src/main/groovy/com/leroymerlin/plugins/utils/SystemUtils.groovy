package com.leroymerlin.plugins.utils

class SystemUtils {

    static String getEnvProperty(String name) {
        String property = null
        if (System.getenv(name) != null && !System.getenv(name).trim().isEmpty()) property = System.getenv(name)
        if (property == null && System.getProperty(name) != null && !System.getProperty(name).trim().isEmpty()) property = System.getProperty(name)
        return property
    }
}
