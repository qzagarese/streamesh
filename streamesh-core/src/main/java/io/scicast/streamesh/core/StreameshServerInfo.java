package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StreameshServerInfo {

    private String host;
    private String ipAddress;
    private int port;
    private WebProtocol protocol;
    private String baseApiPath;

    public enum WebProtocol {
        http, https
    }

    public String getBaseUrl() {
        return protocol.toString() + "://" + host + ":"+ port + baseApiPath;
    }

}
