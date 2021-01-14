package com.trasier.client.opentracing;

import io.opentracing.Scope;

public class TrasierScope implements Scope {
    private TrasierScopeManager scopeManager;
    private TrasierSpan span;

    public TrasierScope(TrasierScopeManager scopeManager, TrasierSpan span) {
        this.scopeManager = scopeManager;
        this.span = span;
    }

    @Override
    public void close() {
        scopeManager.deactivate(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrasierScope that = (TrasierScope) o;
        return span != null ? span.equals(that.span) : that.span == null;
    }

    @Override
    public int hashCode() {
        return span != null ? span.hashCode() : 0;
    }

    public TrasierSpan getSpan() {
        return span;
    }
}
