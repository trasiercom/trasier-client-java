package com.trasier.client.configuration;

public class TrasierClientConfiguration {
    private String accountId;
    private String spaceKey;
    private String clientId;
    private String clientSecret;
    private String systemName;

    private boolean useAuth = true;
    private boolean activated = true;
    private boolean payloadTracingDisabled = false;
    private boolean compressPayloadDisabled = false;
    private long logMetricsInterval = 10 * 60 * 1000;

    private int ahcMaxConnections = 1000;
    private int ahcConnectTimeout = 2500;
    private int ahcReadTimeout = 1000;
    private int ahcRequestTimeout = 1000;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isCompressPayloadDisabled() {
        return compressPayloadDisabled;
    }

    public void setCompressPayloadDisabled(final boolean compressPayloadDisabled) {
        this.compressPayloadDisabled = compressPayloadDisabled;
    }

    public void setPayloadTracingDisabled(boolean payloadTracingDisabled) {
        this.payloadTracingDisabled = payloadTracingDisabled;
    }

    public boolean isPayloadTracingDisabled() {
        return payloadTracingDisabled;
    }

    public long getLogMetricsInterval() {
        return logMetricsInterval;
    }

    public void setLogMetricsInterval(final long logMetricsInterval) {
        this.logMetricsInterval = logMetricsInterval;
    }

    public int getAhcMaxConnections() {
        return ahcMaxConnections;
    }

    public void setAhcMaxConnections(final int ahcMaxConnections) {
        this.ahcMaxConnections = ahcMaxConnections;
    }

    public int getAhcConnectTimeout() {
        return ahcConnectTimeout;
    }

    public void setAhcConnectTimeout(final int ahcConnectTimeout) {
        this.ahcConnectTimeout = ahcConnectTimeout;
    }

    public int getAhcReadTimeout() {
        return ahcReadTimeout;
    }

    public void setAhcReadTimeout(final int ahcReadTimeout) {
        this.ahcReadTimeout = ahcReadTimeout;
    }

    public int getAhcRequestTimeout() {
        return ahcRequestTimeout;
    }

    public void setAhcRequestTimeout(final int ahcRequestTimeout) {
        this.ahcRequestTimeout = ahcRequestTimeout;
    }

    public boolean isUseAuth() {
        return useAuth;
    }

    public void setUseAuth(boolean useAuth) {
        this.useAuth = useAuth;
    }
}