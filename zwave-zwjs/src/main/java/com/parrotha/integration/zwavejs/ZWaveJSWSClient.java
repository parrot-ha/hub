package com.parrotha.integration.zwavejs;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class ZWaveJSWSClient extends WebSocketClient {
    public ZWaveJSWSClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("zwave js onOpen");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("zwave js onMessage: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("zwave js onClose code: " + code + " " + " reason " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("zwave js onError");
        ex.printStackTrace();
    }
}
