package com.trasier.client.opentracing;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

import java.util.ArrayDeque;
import java.util.Deque;

public class TrasierScopeManager implements ScopeManager {
    private final ThreadLocal<Deque<Scope>> scopes = ThreadLocal.withInitial(ArrayDeque::new);

    @Override
    public Scope activate(Span span) {
        TrasierScope scope = new TrasierScope(this, (TrasierSpan) span);
        scopes.get().addFirst(scope);
        return scope;
    }

    @Override
    public Span activeSpan() {
        TrasierScope trasierScope = (TrasierScope) activeScope();
        return trasierScope != null ? trasierScope.getSpan() : null;
    }

    public Scope activeScope() {
        return scopes.get().peekFirst();
    }

    public void deactivate(TrasierScope scope) {
        scopes.get().remove(scope);
    }
}