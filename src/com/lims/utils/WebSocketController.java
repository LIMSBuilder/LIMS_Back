package com.lims.utils;

import javax.servlet.Servlet;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket.ws")
public class WebSocketController {
    @OnOpen
    public void onOpen(Session session) {

    }

    @OnClose
    public void onClose(Session session) {

    }

    @OnMessage
    public void onMessage(String requestJson, Session session) {
        try {
            session.getBasicRemote().sendText(requestJson);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
