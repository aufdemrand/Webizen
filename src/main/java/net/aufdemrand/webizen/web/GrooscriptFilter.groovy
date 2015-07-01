package net.aufdemrand.webizen.web

import net.aufdemrand.webizen.Webizen
import org.grooscript.GrooScript

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse


class GrooscriptFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        response.setContentType('text/javascript')
        try {
            response.getWriter().append(GrooScript.convert(new File(Webizen.static_path + request.getRequestURI().replace('/static', '')).text))
        } catch (Throwable e) {
            response.setStatus(501)
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {  }

    @Override
    public void destroy() {  }

}
