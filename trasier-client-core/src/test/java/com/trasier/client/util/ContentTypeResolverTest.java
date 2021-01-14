package com.trasier.client.util;

import com.trasier.client.api.ContentType;
import org.junit.Test;

import static com.trasier.client.util.ContentTypeResolver.resolveFromPayload;
import static org.junit.Assert.assertEquals;

public class ContentTypeResolverTest {

    @Test
    public void resolveContentType() {
        assertEquals(ContentType.XML, resolveFromPayload("<project></project>"));
        assertEquals(ContentType.JSON, resolveFromPayload("{project:[version: 123]}"));
        assertEquals(ContentType.TEXT, resolveFromPayload("Hello"));
    }

}