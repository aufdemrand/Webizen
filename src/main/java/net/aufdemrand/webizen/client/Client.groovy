package net.aufdemrand.webizen.client

import net.aufdemrand.webizen.database.Document
import net.aufdemrand.webizen.hooks.Hook
import net.aufdemrand.webizen.hooks.HttpResult
import net.aufdemrand.webizen.hooks.Result
import net.aufdemrand.webizen.web.SocketHandler


class Client {

    // Unique identifier between the remote client (user on the other end)
    // and the server client (this), handled server-side by HashSessionManager (Jetty)
    String session_id;

    // The socket used to communicate with the user on the other end, if connected.
    SocketHandler client;

    // The database document associated with the client, if logged in.
    Document doc;


    public Client(session_id) {
        this.session_id = session_id;
    }


    @Hook('on /client/login/ post')
    public login(Result r) {
                
    }




}
