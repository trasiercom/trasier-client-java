package com.trasier.client.spring.context;

import com.trasier.client.api.Span;
import org.springframework.core.NamedThreadLocal;

public class TrasierContextHolder {

    private static final ThreadLocal<TrasierContext> CURRENT_TRASIER_CONTEXT = new NamedThreadLocal<>("TrasierContext");

    static Span getSpan() {
        return isTracing() ? CURRENT_TRASIER_CONTEXT.get().span : null;
    }

    static void setSpan(Span span) {
        push(span);
    }

    static boolean isTracing() {
        return CURRENT_TRASIER_CONTEXT.get() != null;
    }

    static void closeSpan() {
        TrasierContext current = CURRENT_TRASIER_CONTEXT.get();
        CURRENT_TRASIER_CONTEXT.remove();
        if (current != null) {
            TrasierContext parent = current.parent;
            if (parent != null) {
                CURRENT_TRASIER_CONTEXT.set(parent);
            }
        }
    }

    static void clear() {
        CURRENT_TRASIER_CONTEXT.remove();
    }

    private static void push(Span span) {
        if (isCurrent(span)) {
            return;
        }
        CURRENT_TRASIER_CONTEXT.set(new TrasierContext(span));
    }

    private static boolean isCurrent(Span span) {
        if (span == null || CURRENT_TRASIER_CONTEXT.get() == null) {
            return false;
        }
        return span.equals(CURRENT_TRASIER_CONTEXT.get().span);
    }

    private static class TrasierContext {
        Span span;
        TrasierContext parent;

        TrasierContext(Span span) {
            this.span = span;
            this.parent = CURRENT_TRASIER_CONTEXT.get();
        }
    }
}