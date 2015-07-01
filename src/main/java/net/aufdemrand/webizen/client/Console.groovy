package net.aufdemrand.webizen.client

import groovy.json.JsonBuilder
import net.aufdemrand.webizen.hooks.HttpResult
import net.aufdemrand.webizen.hooks.types.Hook


/**
 * Created by Jeremy on 5/31/2015.
 */
class Console {

    static def history = [:]

    static def clients = []

    static report(def report) {
        // object to store in report history
        def reported
        // handle simple string report but allow for
        // more complex report
        if (report instanceof String) {
            reported = ['value' : report]
        } else reported = report;
        // report keyed by time
        def time = System.currentTimeMillis();
        reported['time'] = System.currentTimeMillis()
        // keys contain a list of values
        if (!history.containsKey(time))
            history[time] = []
        // add this report history
        (history[time] as List).add(reported)
    }


    @Hook('on /service-bay/console-submit/ hit')
    public static submitConsole(HttpResult r) {
        // validate user
        println r.getClient()
        def shell = new GroovyShell();
        Script s;
        // try to compile
        try {
            shell.setProperty('response', r.response)
            shell.setProperty('request', r.request)
            s = shell.parse(r.request.getParameter('data'))
        } catch (Exception e) {
            r.response.getWriter().println('{"status":"error","response":"Could not compile script."}')
            return;
        }
        def returned;
        // try to execute
        try { returned = s.run() } catch (Exception e) {
            r.response.getWriter().println('{"status":"error","response":"Could not execute script."}')
            return;
        }
        // send response
        r.response.getWriter().println(new JsonBuilder([
                    'status' : 'ok',
                    'response' : returned
                ]).toPrettyString());
    }



}
