package net.aufdemrand.webizen.web

import groovy.json.JsonSlurper
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import net.aufdemrand.webizen.sockets.Socket
import org.eclipse.jetty.websocket.api.CloseStatus
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import java.lang.ref.WeakReference

public class SocketHandler extends WebSocketAdapter {

    def flags = new ConfigObject()



    @Override
    public void onWebSocketConnect(Session sess) {

        super.onWebSocketConnect(sess);

        println sess

        def context = [
                'handler' : this,
                'session' : session,
                'time'    : System.currentTimeMillis(),
                'flags'   : flags
        ]

        // Allow socket connections (intial contact) to be monitored/refused
        Result o = Hooks.invoke('on socket connect', context)
        if (o.cancelled) {
            sess.close()
        };

    }


    @Override
    public void onWebSocketText(String message) {

        // 'message' will be dealt with via a JSON structure
        def json = new JsonSlurper().parseText(message)

        // Must have a 'handler-id' of the socket handler
        if (json['handler-id'] == null) return;

        def context = [
                'handler'    : this,
                'session'    : session,
                'time'       : System.currentTimeMillis(),
                'args'       : json,
                'handler-id' : json['handler-id'],
                'flags'      : flags
        ]

        Result o = Hooks.invoke('on socket data', context)
        if (o.cancelled) return;

        Hooks.invoke('on socket ' + json['handler-id'], context)
    }


    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode,reason);

        // invoke hook for socket closed
        def context = [
                'handler'    : this,
                'session'    : session,
                'time'       : System.currentTimeMillis(),
                'flags'      : flags
        ]

        Hooks.invoke('on socket closed', context)
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        // cause.printStackTrace(System.err);
    }
}