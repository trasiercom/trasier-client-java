package com.trasier.client.interceptor;

import com.trasier.client.api.Span;
import org.iq80.snappy.Snappy;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Base64;

public class TrasierCompressSpanInterceptor {

    public static final String INCOMING_DATA_COMPRESSION = "INCOMING_DATA_COMPRESSION";
    public static final String OUTGOING_DATA_COMPRESSION = "OUTGOING_DATA_COMPRESSION";

    private static final int PAYLOAD_LIMIT_BYTES = 940_000;
    private static final byte[] PAYLOAD_TOO_BIG = "PAYLOAD_TOO_BIG".getBytes();
    private static final byte[] EMPTY = new byte[0];
    private static final String SPAN_KIND_SERVER = "server";
    private static final String SNAPPY = "SNAPPY";

    private int payloadLimitBytes = PAYLOAD_LIMIT_BYTES;
    private boolean truncateMessage = false;

    public void intercept(Span span) {
        // sanity check
        if (span.isPayloadDisabled()) {
            span.setOutgoingData(null);
            span.setIncomingData(null);
        } else {
            String incomingData = span.getIncomingData();
            String outgoingData = span.getOutgoingData();

            byte[] incomingDataBytes = incomingData != null && incomingData.trim().length() > 0 && !span.getTags().containsKey(INCOMING_DATA_COMPRESSION) ? Snappy.compress(incomingData.getBytes()) : EMPTY;
            byte[] outgoingDataBytes = outgoingData != null && outgoingData.trim().length() > 0 && !span.getTags().containsKey(OUTGOING_DATA_COMPRESSION) ? Snappy.compress(outgoingData.getBytes()) : EMPTY;
            int totalBytes = incomingDataBytes.length + outgoingDataBytes.length;

            if (incomingDataBytes.length > 0) {
                if (totalBytes > this.payloadLimitBytes) {
                    if (incomingDataBytes.length > this.payloadLimitBytes || (isServer(span) && outgoingDataBytes.length < this.payloadLimitBytes)) {
                        incomingDataBytes = Snappy.compress(truncateMessage ? truncate(incomingData.getBytes()) : PAYLOAD_TOO_BIG);
                    }
                }
                String compressedData = Base64.getEncoder().encodeToString(incomingDataBytes);
                span.setIncomingData(compressedData);
                span.getTags().put(INCOMING_DATA_COMPRESSION, SNAPPY);
            }

            if (outgoingDataBytes.length > 0) {
                if (totalBytes > this.payloadLimitBytes) {
                    if (outgoingDataBytes.length > this.payloadLimitBytes || (!isServer(span) && incomingDataBytes.length < this.payloadLimitBytes)) {
                        outgoingDataBytes = Snappy.compress(truncateMessage ? truncate(outgoingData.getBytes()) : PAYLOAD_TOO_BIG);
                    }
                }
                String compressedData = Base64.getEncoder().encodeToString(outgoingDataBytes);
                span.setOutgoingData(compressedData);
                span.getTags().put(OUTGOING_DATA_COMPRESSION, SNAPPY);
            }
        }
    }

    private byte[] truncate(byte[] bytes) {
        try {
            Charset CHARSET = Charset.forName("UTF-8");
            CharsetDecoder decoder = CHARSET.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
            decoder.reset();
            int limit = bytes.length < this.payloadLimitBytes ? bytes.length : this.payloadLimitBytes;
            CharBuffer decoded = decoder.decode(ByteBuffer.wrap(bytes, 0, limit));
            return (decoded.toString() + "... MESSAGE TRUNCATED, PAYLOAD TOO BIG").getBytes();
        } catch (Exception e) {
            return PAYLOAD_TOO_BIG;
        }
    }

    public void setPayloadLimitBytes(int payloadLimitBytes) {
        this.payloadLimitBytes = payloadLimitBytes;
    }

    public void setTruncateMessage(boolean truncateMessage) {
        this.truncateMessage = truncateMessage;
    }

    private boolean isServer(Span span) {
        return SPAN_KIND_SERVER.equals(span.getTags().get("span.kind"));
    }

}
