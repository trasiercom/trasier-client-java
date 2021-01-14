package com.trasier.client.spring.client;

import com.trasier.client.api.Span;
import com.trasier.client.interceptor.TrasierCompressSpanInterceptor;
import org.iq80.snappy.Snappy;
import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TrasierCompressSpanInterceptorTest {

    @Test
    public void testNoPayload() {
        // given
        TrasierCompressSpanInterceptor sut = new TrasierCompressSpanInterceptor();
        sut.setPayloadLimitBytes(100);

        Span span = Span.newSpan("truncate", "id1", "id2", "id3")
                .incomingData(null)
                .outgoingData(null)
                .build();

        // when
        sut.intercept(span);

        // then
        assertNull( span.getIncomingData());
        assertNull( span.getOutgoingData());
    }

    @Test
    public void testEmptyPayload() {
        // given
        TrasierCompressSpanInterceptor sut = new TrasierCompressSpanInterceptor();
        sut.setPayloadLimitBytes(100);

        Span span = Span.newSpan("truncate", "id1", "id2", "id3")
                .incomingData("")
                .outgoingData("")
                .build();

        // when
        sut.intercept(span);

        // then
        assertEquals("",span.getIncomingData());
        assertEquals("", span.getOutgoingData());
    }



    @Test
    public void testCompress() {
        // given
        TrasierCompressSpanInterceptor sut = new TrasierCompressSpanInterceptor();
        sut.setPayloadLimitBytes(100);

        Span span = Span.newSpan("truncate", "id1", "id2", "id3")
                .incomingData(createMessage(10))
                .outgoingData(createMessage(20))
                .build();

        // when
        sut.intercept(span);

        // then
        assertEquals("BOOM BOOM", decodeString(span.getIncomingData()).trim());
        assertEquals("BOOM BOOM BOOM BOOM", decodeString(span.getOutgoingData()).trim());
    }

    @Test
    public void testTruncateMessage() {
        // given
        TrasierCompressSpanInterceptor sut = new TrasierCompressSpanInterceptor();
        sut.setPayloadLimitBytes(15);
        sut.setTruncateMessage(true);

        Span span = Span.newSpan("truncate", "id1", "id2", "id3")
                .incomingData(createMessage(10))
                .outgoingData(createMessage(20))
                .build();

        // when
        sut.intercept(span);

        // then
        assertEquals("BOOM BOOM", decodeString(span.getIncomingData()).trim());
        assertEquals("BOOM BOOM BOOM ... MESSAGE TRUNCATED, PAYLOAD TOO BIG", decodeString(span.getOutgoingData()));
    }

    @Test
    public void testTruncateMessageBothTooBig() {
        // given
        TrasierCompressSpanInterceptor sut = new TrasierCompressSpanInterceptor();
        sut.setPayloadLimitBytes(15);
        sut.setTruncateMessage(true);

        Span span = Span.newSpan("truncate", "id1", "id2", "id3")
                .incomingData(createMessage(20))
                .outgoingData(createMessage(20))
                .build();

        // when
        sut.intercept(span);

        // then
        assertEquals("BOOM BOOM BOOM ... MESSAGE TRUNCATED, PAYLOAD TOO BIG", decodeString(span.getIncomingData()).trim());
        assertEquals("BOOM BOOM BOOM ... MESSAGE TRUNCATED, PAYLOAD TOO BIG", decodeString(span.getOutgoingData()));
    }

    @Test
    public void testTruncateOutgoinglient() {
        // given
        TrasierCompressSpanInterceptor sut = new TrasierCompressSpanInterceptor();
        sut.setPayloadLimitBytes(15);
        sut.setTruncateMessage(true);

        Span span = Span.newSpan("truncate", "id1", "id2", "id3")
                .incomingData(createMessage(10))
                .outgoingData(createMessage(10))
                .build();

        span.getTags().put("span.kind", "client");

        // when
        sut.intercept(span);

        // then
        assertEquals("BOOM BOOM", decodeString(span.getIncomingData()).trim());
        assertEquals("BOOM BOOM ... MESSAGE TRUNCATED, PAYLOAD TOO BIG", decodeString(span.getOutgoingData()).trim());
    }

    @Test
    public void testTruncateIncomingServer() {
        // given
        TrasierCompressSpanInterceptor sut = new TrasierCompressSpanInterceptor();
        sut.setPayloadLimitBytes(15);
        sut.setTruncateMessage(true);

        Span span = Span.newSpan("truncate", "id1", "id2", "id3")
                .incomingData(createMessage(10))
                .outgoingData(createMessage(10))
                .build();

        span.getTags().put("span.kind", "server");

        // when
        sut.intercept(span);

        // then
        assertEquals("BOOM BOOM ... MESSAGE TRUNCATED, PAYLOAD TOO BIG", decodeString(span.getIncomingData()).trim());
        assertEquals("BOOM BOOM", decodeString(span.getOutgoingData()).trim());
    }

    @Test
    public void testDropMessageBothTooBig() {
        // given
        TrasierCompressSpanInterceptor sut = new TrasierCompressSpanInterceptor();
        sut.setPayloadLimitBytes(15);

        Span span = Span.newSpan("truncate", "id1", "id2", "id3")
                .incomingData(createMessage(20))
                .outgoingData(createMessage(20))
                .build();

        // when
        sut.intercept(span);

        // then
        assertEquals("PAYLOAD_TOO_BIG", decodeString(span.getIncomingData()).trim());
        assertEquals("PAYLOAD_TOO_BIG", decodeString(span.getOutgoingData()));
    }

    private String decodeString(String data) {
        byte[] decodeOutgoing = Base64.getDecoder().decode(data);
        byte[] uncompress = Snappy.uncompress(decodeOutgoing, 0, decodeOutgoing.length);
        return new String(uncompress);
    }

    private String createMessage(int byteSize) {
        String message = "BOOM ";
        while (message.getBytes().length < byteSize) {
            message += message;
        }
        return message;
    }
}