package com.trasier.client.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {

    public static String getString(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
