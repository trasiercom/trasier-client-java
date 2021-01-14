package com.trasier.client.utils;

public class Precondition {

    public static <T> T  notNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("Argument " + fieldName + " cannot be null.");
        }
        return value;
    }

    public static String notBlank(String value, String fieldName) {
        if (value == null || value.trim().length() < 1) {
            throw new IllegalArgumentException("Argument " + fieldName + " cannot be empty.");
        }
        return value;
    }

}
