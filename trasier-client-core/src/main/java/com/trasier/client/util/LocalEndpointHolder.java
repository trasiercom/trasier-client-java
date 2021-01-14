package com.trasier.client.util;

import com.trasier.client.api.Endpoint;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalEndpointHolder {

    private static Endpoint localEndpoint;

    public static Endpoint getLocalEndpoint(String systemName) {
        // no synchronisation on purpose
        if (localEndpoint == null) {
            Endpoint endpoint = new Endpoint(systemName);
            InetAddress inetAddress = getInetAddress();
            if (inetAddress != null) {
                endpoint.setHostname(inetAddress.getHostName());
                endpoint.setIpAddress(inetAddress.getHostAddress());
            }
            localEndpoint = endpoint;
        }
        return localEndpoint;
    }

    private static InetAddress getInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // ignore
        }
        return null;
    }

}
