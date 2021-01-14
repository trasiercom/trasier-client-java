package com.trasier.client.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Endpoint {
    private String name;
    private String ipAddress;
    private String port;
    private String hostname;

    public Endpoint(String name) {
        this.name = name;
    }
}
