package net.aufdemrand.webizen.web

import net.aufdemrand.webizen.client.Client
import net.aufdemrand.webizen.client.ClientRegistry
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.HttpResult
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by Jeremy on 1/15/2015.
 */
class RequestHandler extends AbstractHandler {


    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)

            throws IOException, ServletException {

        // For now, just handle _everything_ as OK text/HTML, unless otherwise set.
        baseRequest.setHandled(true);


        // Have to catch all errors here or else Jetty seems to catch and hide
        // them itself.

        try {

            // Disable page caching by default
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            // And allow cross-domain
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type");

            // OPTIONS? Return with a '200' response -- some browsers send this before an actual
            // request to get header contents.
            if (request.getMethod() == "OPTIONS") {
                response.setStatus(200)
                return;
            }

            def context = [
                    // intended to be immutable
                    'request'  : request,
                    'response' : response,
                    'client'   : ClientRegistry.getClient(request.getSession().getId()),
                    'hit-time' : System.currentTimeMillis(),
                    // intended to be mutable
                    'content-type' : "text/html;charset=utf-8",
                    'status'   : 200,
                    // Specify subclass of result type (helper methods)
                    'result-type': HttpResult.class
            ]

            Hooks.invoke('on page hit', context)
            Hooks.invoke('on ' + target + ' hit', context)

            // Use Result to set status (default 200), contentType, etc.
            // Anything more complex should alter the response itself,
            // for example -- Headers/etc.
            response.setStatus(context['status'] as Integer)
            response.setContentType(context['content-type'] as String)

        } catch (Exception e) { e.printStackTrace(); response.setStatus(501); }

    }


}
