package net.aufdemrand.webizen.web

import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import net.aufdemrand.webizen.sockets.Socket
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import java.lang.ref.WeakReference

public class SocketHandler extends WebSocketAdapter
{

    def flags = new ConfigObject();

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
    }

    @Override
    public void onWebSocketText(String message) {
        // validating?
        if (message.startsWith('val')) {
            def context = [
                    'handler' : this,
                    'session' : session,
                    'time'    : System.currentTimeMillis(),
                    'id'      : message.substring(4),
                    'flags'   : flags
            ]
            Result r = Hooks.invoke('on socket validation', context)
            if (r.cancelled) {
                session.getRemote().sendString("log Validation denied.")
                return
            }
            Socket.sessions[message.substring(4)] = this;
            flags['id'] = message.substring(4);
            session.getRemote().sendString("log Validated ${message.substring(4)}.")
            return
        }
        // make sure validated
        if (flags['id'] == null) {
            session.getRemote().sendString("log Not validated.")
            def context = [
                    'handler' : this,
                    'session' : session,
                    'time'    : System.currentTimeMillis(),
                    'id'      : message.substring(4),
                    'flags'   : flags
            ]
            Hooks.invoke('on socket not validated error', context)
            return
        }
        // else, rely on hooks to perform actions
        def command = message.split('\\s', 2)
        def context = [
                'handler' : this,
                'session' : session,
                'time'    : System.currentTimeMillis(),
                'id'      : message.substring(4),
                'flags'   : flags,
                'command' : command[0],
                'args'    : command[1]
        ]
        Result o = Hooks.invoke('on socket invoked', context)
        if (o.cancelled) return;
        Hooks.invoke('on socket ' + command[0], context)
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode,reason);
        // unregister session
        Socket.sessions.remove(flags['id'])
        // invoke hook for socket closed
        def context = [
                'handler' : this,
                'session' : session,
                'time'    : System.currentTimeMillis(),
                'id'      : flags['id'],
                'flags'   : flags
        ]
        Hooks.invoke('on socket closed', context)
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }
}