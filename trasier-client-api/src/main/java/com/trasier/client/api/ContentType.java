package com.trasier.client.api;

public enum ContentType {

    TEXT("text/plain"),
    XML("application/xml"),
    JSON("application/json"),
    SQL("application/sql"),
    BINARY("application/octet-stream"),
    ENCRYPTED("application/pgp-encrypted");

    private String mimeType;

    ContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}