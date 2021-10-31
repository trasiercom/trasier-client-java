package com.trasier.client.opentelemetry;

import com.trasier.client.configuration.TrasierClientConfiguration;
import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

public final class TrasierSpanExporterBuilder {
  private String endpoint;
  private TrasierClientConfiguration clientConfiguration;

  public TrasierSpanExporterBuilder setEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  public TrasierSpanExporterBuilder setClientConfiguration(TrasierClientConfiguration clientConfiguration) {
    this.clientConfiguration = clientConfiguration;
    return this;
  }

  public TrasierSpanExporter build() {
    return new TrasierSpanExporter(this.encoder, this.sender);
  }
}