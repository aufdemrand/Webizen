package net.aufdemrand.webizen.hooks

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by Jeremy on 5/31/2015.
 */
class HttpResult extends Result {

    boolean cancelled = false

    def context = [:]

    public HttpServletRequest request = {
        return context['request']
    } as HttpServletRequest

    public HttpServletResponse response = {
        return context['response']
    } as HttpServletResponse

    public String session_id = {
        return request.getSession().getId()
    } as String

    public Long hit_time = {
        return context['hit_time']

    } as Long

    HttpResult(Object context) {
        super(context)
    }
}
