package net.aufdemrand.webizen.client

import groovy.json.JsonBuilder
import net.aufdemrand.webizen.hooks.HttpResult
import net.aufdemrand.webizen.hooks.Result
import net.aufdemrand.webizen.hooks.types.Hook

/**
 * Created by Jeremy on 6/15/2015.
 */
class ClientRegistry {


    public static activeClients = []


    public static boolean isActive(String sessionId) {
        // Internal
        for (Client client in activeClients) {
            if (client.session_id == sessionId)
                return true
        }
        return false
    }


    public static void registerClient(Client c) {
        // Internal
        activeClients.add(c)
    }


    public static Client getClient(String sessionId) {
        for (Client client in activeClients) {
            if (client.session_id == sessionId)
            return client
        }
        // None found? Create a new Client.
        return new Client(sessionId)
    }


    @Hook('on /service-bay/get-clients/ hit')
    public static listClients(HttpResult r) {
        r.response.getWriter().append( new JsonBuilder(activeClients).toPrettyString() )
    }


}
