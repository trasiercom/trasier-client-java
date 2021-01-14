package com.trasier.client.spring.grpc;

import com.trasier.api.client.protobuf.SpanRequest;
import com.trasier.api.client.protobuf.SpanResponse;
import com.trasier.api.client.protobuf.WriteServiceGrpc;
import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.interceptor.TrasierCompressSpanInterceptor;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.client.TrasierSpringClient;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.List;

@Component("trasierSpringClient")
public class TrasierSpringGrpcClient implements TrasierSpringClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierSpringGrpcClient.class);

    private final TrasierClientConfiguration clientConfiguration;

    private ManagedChannel channel;
    private WriteServiceGrpc.WriteServiceStub stub;
    private GrpcSpanConverter converter;

    private TrasierCompressSpanInterceptor compressSpanInterceptor;

    @Autowired(required = false)
    private final List<TrasierSpanInterceptor> spanInterceptors = new ArrayList<>();

    @Autowired
    public TrasierSpringGrpcClient(TrasierClientConfiguration clientConfiguration, TrasierEndpointConfiguration applicationConfiguration, TrasierAuthClientInterceptor authClientInterceptor) throws SSLException {
        this.clientConfiguration = clientConfiguration;

        if (clientConfiguration.isActivated()) {
            NettyChannelBuilder channelBuilder = NettyChannelBuilder.forTarget(applicationConfiguration.getGrpcEndpoint())
                    .compressorRegistry(CompressorRegistry.getDefaultInstance())
                    .decompressorRegistry(DecompressorRegistry.getDefaultInstance());

            channelBuilder.useTransportSecurity()
                    .sslContext(GrpcSslContexts.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE).build());

            this.channel = channelBuilder.build();
            this.stub = WriteServiceGrpc.newStub(channel).withInterceptors(authClientInterceptor);
            this.converter = new GrpcSpanConverter();

            if (!clientConfiguration.isCompressPayloadDisabled()) {
                this.compressSpanInterceptor = new TrasierCompressSpanInterceptor();
            }
        }
    }

    @Override
    public boolean sendSpan(Span span) {
        if (!clientConfiguration.isActivated()) {
            return false;
        }

        if (span.isCancel()) {
            return false;
        }

        applyInterceptors(span);

        if (span.isCancel()) {
            return false;
        }

        if(compressSpanInterceptor != null) {
            compressSpanInterceptor.intercept(span);
        }

        SpanRequest.Builder builder = SpanRequest.newBuilder();
        builder.setAccountId(clientConfiguration.getAccountId());
        builder.setSpaceKey(clientConfiguration.getSpaceKey());
        builder.addSpans(converter.convert(span));

        sendStreams(builder.build());

        return true;
    }

    private void sendStreams(SpanRequest spanRequest) {
        StreamObserver<SpanRequest> requestObserver = stub.send(new StreamObserver<SpanResponse>() {
            @Override
            public void onNext(SpanResponse o) {
                // nothing
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("Spans not written.", throwable);
            }

            @Override
            public void onCompleted() {
                LOGGER.trace("Spans submitted to tracing backend successfully");
            }
        });

        try {
            requestObserver.onNext(spanRequest);
        } catch (Exception e) {
            requestObserver.onError(e);
        } finally {
            requestObserver.onCompleted();
        }
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        if (!clientConfiguration.isActivated()) {
            return false;
        }

        SpanRequest.Builder builder = SpanRequest.newBuilder();
        builder.setAccountId(clientConfiguration.getAccountId());
        builder.setSpaceKey(clientConfiguration.getSpaceKey());

        spans.stream()
                .filter(span -> !span.isCancel())
                .peek(this::applyInterceptors)
                .filter(span -> !span.isCancel())
                .peek(span -> {
                    if(compressSpanInterceptor != null) {
                        compressSpanInterceptor.intercept(span);
                    }
                })
                .map(converter::convert)
                .forEach(builder::addSpans);

        if (builder.getSpansCount() > 0) {
            sendStreams(builder.build());
            return true;
        } else {
            return false;
        }
    }

    private void applyInterceptors(Span span) {
        for (TrasierSpanInterceptor spanInterceptor : this.spanInterceptors) {
            spanInterceptor.intercept(span);
        }
    }

    @Override
    public void close() {
        channel.shutdownNow();
    }

    public void setCompressSpanInterceptor(TrasierCompressSpanInterceptor compressSpanInterceptor) {
        this.compressSpanInterceptor = compressSpanInterceptor;
    }

}