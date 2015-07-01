package net.aufdemrand.webizen.web

import net.aufdemrand.webizen.Webizen
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.session.HashSessionIdManager
import org.eclipse.jetty.server.session.HashSessionManager
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

import javax.servlet.DispatcherType


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

        org.eclipse.jetty.server.Server server =
                new org.eclipse.jetty.server.Server(http_port)

        // Create Session Manager
        HashSessionIdManager id_manager = new HashSessionIdManager()
        server.setSessionIdManager(id_manager)
        HashSessionManager manager = new HashSessionManager()
        SessionHandler sessions = new SessionHandler(manager)

        // Handlers we will use
        HandlerCollection handlers = new HandlerCollection()

        // Socket handler
        ContextHandler sockets = new ContextHandler()
        sockets.setContextPath("/sockets/*")
        sockets.setHandler(sessions)
        sessions.setHandler(new SocketFactory())
        handlers.addHandler(sessions)

        // Static files handler (DefaultServlet allows file modification/filters)
        ServletContextHandler resources = new ServletContextHandler()
        resources.setResourceBase(Webizen.static_path)
        resources.setContextPath('/static/*')
        resources.addFilter(GrooscriptFilter.class, "*.groovy", EnumSet.of(DispatcherType.REQUEST))
        DefaultServlet resourceServlet = new DefaultServlet();
        ServletHolder servletHolder = new ServletHolder(resourceServlet);
        servletHolder.setInitParameter('useFileMappedBuffer', 'false');
        resources.addServlet(servletHolder, '/')
        handlers.addHandler(resources)

        // Scripts handler
        ContextHandler scripts = new ContextHandler()
        scripts.setContextPath("/")
        scripts.setHandler(new RequestHandler())
        handlers.addHandler(scripts)


        server.setHandler(handlers)

        // Start the server!
        try {
            server.start()
            server.join()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }



}




