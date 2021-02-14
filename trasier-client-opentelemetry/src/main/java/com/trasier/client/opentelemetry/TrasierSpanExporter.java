package com.trasier.client.opentelemetry;

import com.trasier.client.api.Client;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public final class TrasierSpanExporter implements SpanExporter {

    private static final Logger logger = Logger.getLogger(TrasierSpanExporter.class.getName());

    private Client client;
    private TrasierClientConfiguration configuration;

    public TrasierSpanExporter(Client client, TrasierClientConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    public CompletableResultCode export(final Collection<SpanData> spanDataList) {
        List<Span> spans = new ArrayList<>(spanDataList.size());
        for (SpanData spanData : spanDataList) {
            spans.add(convertSpan(spanData));
        }

        final CompletableResultCode result = new CompletableResultCode();

        client.sendSpans(spans);

        return result;
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        client.close();
        return CompletableResultCode.ofSuccess();
    }

    static final String OTEL_STATUS_CODE = "otel.status_code";

    static final String KEY_INSTRUMENTATION_LIBRARY_NAME = "otel.library.name";
    static final String KEY_INSTRUMENTATION_LIBRARY_VERSION = "otel.library.version";

    Span convertSpan(SpanData spanData) {
        Endpoint endpoint = getEndpoint(spanData);

        long startTimestamp = toEpochMicros(spanData.getStartEpochNanos());
        long endTimestamp = toEpochMicros(spanData.getEndEpochNanos());

        final Span.SpanBuilder spanBuilder =
                Span.newSpan(spanData.getName(), UUID.randomUUID().toString(), spanData.getTraceId(), spanData.getSpanId())
                        //.kind(toSpanKind(spanData))
                        .startTimestamp(startTimestamp)
                        .endTimestamp(endTimestamp);

        if (spanData.getKind() == SpanKind.CLIENT || spanData.getKind() == SpanKind.PRODUCER) {
            spanBuilder.incomingEndpoint(endpoint);
        }

        if (spanData.getKind() == SpanKind.SERVER || spanData.getKind() == SpanKind.CONSUMER) {
            spanBuilder.outgoingEndpoint(endpoint);
        }

        if (spanData.getParentSpanContext().isValid()) {
            spanBuilder.parentId(spanData.getParentSpanId());
        }
/*
    Attributes spanAttributes = spanData.getAttributes();
    spanAttributes.forEach(
        (key, value) -> spanBuilder.putTag(key.getKey(), valueToString(key, value)));
    StatusData status = spanData.getStatus();

    // include status code & error.
    if (status.getStatusCode() != StatusCode.UNSET) {
      spanBuilder.putTag(OTEL_STATUS_CODE, status.getStatusCode().toString());

      // add the error tag, if it isn't already in the source span.
      if (status.getStatusCode() == StatusCode.ERROR && spanAttributes.get(STATUS_ERROR) == null) {
        spanBuilder.putTag(STATUS_ERROR.getKey(), nullToEmpty(status.getDescription()));
      }
    }

    InstrumentationLibraryInfo instrumentationLibraryInfo =
        spanData.getInstrumentationLibraryInfo();

    if (!instrumentationLibraryInfo.getName().isEmpty()) {
      spanBuilder.putTag(KEY_INSTRUMENTATION_LIBRARY_NAME, instrumentationLibraryInfo.getName());
    }
    if (instrumentationLibraryInfo.getVersion() != null) {
      spanBuilder.putTag(
          KEY_INSTRUMENTATION_LIBRARY_VERSION, instrumentationLibraryInfo.getVersion());
    }

    for (EventData annotation : spanData.getEvents()) {
      spanBuilder.addAnnotation(toEpochMicros(annotation.getEpochNanos()), annotation.getName());
    }
*/
        return spanBuilder.build();
    }

    private Endpoint getEndpoint(SpanData spanData) {
        Attributes resourceAttributes = spanData.getResource().getAttributes();

        // use the service.name from the Resource, if it's been set.
        String serviceNameValue = resourceAttributes.get(ResourceAttributes.SERVICE_NAME);
        if (serviceNameValue == null) {
            serviceNameValue = Resource.getDefault().getAttributes().get(ResourceAttributes.SERVICE_NAME);
        }
        return new Endpoint(serviceNameValue);
    }


    private static long toEpochMicros(long epochNanos) {
        return NANOSECONDS.toMicros(epochNanos);
    }
/*
  private static <T> String valueToString(AttributeKey<?> key, Object attributeValue) {
    AttributeType type = key.getType();
    switch (type) {
      case STRING:
      case BOOLEAN:
      case LONG:
      case DOUBLE:
        return String.valueOf(attributeValue);
      case STRING_ARRAY:
      case BOOLEAN_ARRAY:
      case LONG_ARRAY:
      case DOUBLE_ARRAY:
        return commaSeparated((List<?>) attributeValue);
    }
    throw new IllegalStateException("Unknown attribute type: " + type);
  }

  private static String commaSeparated(List<?> values) {
    StringBuilder builder = new StringBuilder();
    for (Object value : values) {
      if (builder.length() != 0) {
        builder.append(',');
      }
      builder.append(value);
    }
    return builder.toString();
  }
*/
}