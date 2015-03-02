package net.aufdemrand.webizen.sockets

import net.aufdemrand.webizen.web.SocketHandler
import java.lang.ref.WeakReference


class Socket {

    static def sessions = [:]

    static SocketHandler get(String id) {
        return (sessions.get(id) as WeakReference).get() as SocketHandler
    }

    static boolean isAlive(String id) {
        return sessions.containsKey(id)
    }
}
