package com.trasier.opentracing.spring.interceptor.servlet;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GzipUtilTest {

    @Test
    public void testDecompressionEmpty() {
        // given
        byte[] empty = new byte[0];

        // when
        byte[] decompressedString = GzipUtil.decompress(empty);

        // then
        assertFalse(GzipUtil.isGzipStream(empty));
        assertFalse(GzipUtil.isGzipStream(decompressedString));
        assertEquals(0, decompressedString.length);
    }

    @Test
    public void testDecompression() throws IOException {
        // given
        String data = "To be, or not to be...";
        byte[] compressedString = compress(data);

        // when
        byte[] decompressedString = GzipUtil.decompress(compressedString);

        // then
        assertTrue(GzipUtil.isGzipStream(compressedString));
        assertFalse(GzipUtil.isGzipStream(decompressedString));
        assertEquals(data, new String(decompressedString));
    }

    private byte[] compress(String data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes());
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }
}