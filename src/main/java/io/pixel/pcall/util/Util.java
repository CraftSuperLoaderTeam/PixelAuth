package io.pixel.pcall.util;

import java.io.InputStream;

public class Util {
    public static InputStream getResource(String path) {
        String pathx;
        if (path.startsWith("/")) pathx = path;
        else pathx = "/" + path;
        return Util.class.getResourceAsStream(pathx);
    }

    public static int dynamic(int value) {
        return value;
    }

    public static <T> T dynamic(T value) {
        return value;
    }
}
