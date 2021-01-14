package com.trasier.client.api;

import java.util.List;

public interface Client {

    boolean sendSpan(Span span);

    boolean sendSpans(List<Span> spans);

    void close();
}