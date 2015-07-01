package net.aufdemrand.webizen.hooks

import net.aufdemrand.webizen.client.Client

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by Jeremy on 5/31/2015.
 */
class HttpResult extends Result {

    boolean cancelled = false

    def context = [:]

    public HttpServletRequest getRequest() {
        return context['request'] as HttpServletRequest
    }

    public HttpServletResponse getResponse() {
        return context['response'] as HttpServletResponse
    }

    public Client getClient() {
        return context['client'] as Client
    }

    public Long getHitTime() {
        return context['hit_time'] as Long
    }

    HttpResult(Object context) {
        super(context)
    }
}
