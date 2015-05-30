package net.aufdemrand.webizen.web

import org.eclipse.jetty.websocket.server.WebSocketHandler
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory

public class SocketFactory extends WebSocketHandler
{
    @Override
    public void configure(WebSocketServletFactory factory)
    {
        factory.getPolicy().setIdleTimeout(300000)
        factory.register(SocketHandler.class)
    }



}