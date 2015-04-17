package net.aufdemrand.webizen.sockets

import net.aufdemrand.webizen.web.SocketHandler


class Socket {

    static def sessions = [:]

    static SocketHandler get(String id) {
        sessions.entrySet();
        return sessions.get(id) as SocketHandler
    }

    static boolean isAlive(String id) {
        return sessions.containsKey(id)
    }


}
