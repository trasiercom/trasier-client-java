package com.trasier.opentracing.spring.interceptor.servlet;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class GzipUtil {

    public static boolean isGzipStream(byte[] bytes) {
        return bytes.length > 1 && GZIPInputStream.GZIP_MAGIC == ((int) bytes[0] & 0xff | bytes[1] << 8 & 0xff00);
    }

    public static byte[] decompress(byte[] compressed) {
        if (compressed.length > 0) {
            try {
                return safeDecompress(compressed);
            } catch (IOException e) {
                return ("Error while decompressing gzip: " + e.getMessage()).getBytes();
            }
        }
        return compressed;
    }

    private static byte[] safeDecompress(byte[] compressed) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        return IOUtils.toByteArray(gzipInputStream);
    }
}
