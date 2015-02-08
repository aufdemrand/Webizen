package net.aufdemrand.webizen.web

import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.session.HashSessionIdManager
import org.eclipse.jetty.server.session.HashSessionManager
import org.eclipse.jetty.server.session.SessionHandler

/**
 * Created by Jeremy on 1/15/2015.
 */
class Server implements Runnable {


    static public int http_port
    static public def public_url


    public Server(int port, String url ) {
        public_url = url + ":" + port + '/';
        http_port = port;
    }


    @Override
    public void run() {
        //
        // Create a server object.
        //

        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(http_port);

        // Specify the Session ID Manager
        HashSessionIdManager id_manager = new HashSessionIdManager();
        server.setSessionIdManager(id_manager);
        ContextHandler context = new ContextHandler("/");
        server.setHandler(context);
        HashSessionManager manager = new HashSessionManager();
        SessionHandler sessions = new SessionHandler(manager);
        context.setHandler(sessions);

        // Hello, World!
        RequestHandler handler = new RequestHandler();
        sessions.setHandler(handler);

        // Start the server!
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
