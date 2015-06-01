package net.aufdemrand.webizen.client

/**
 * Created by Jeremy on 5/31/2015.
 */
class Console {

    static def history = [:]

    static report(def report) {
        def reported;
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



}
