package net.aufdemrand.webizen.scripts

import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import org.apache.commons.lang.StringUtils


class WebScript extends Script {

    // Web scripts combine a Groovy/HTML/CSS/Javascript stack to provide web page output

    public static Script get(def id) {
        Script d = CouchHandler.couch.getDoc(id, 'scripts', WebScript.class);
        if (d == null) {
            CouchHandler.couch.addDoc(id, 'scripts');
            d = CouchHandler.couch.getDoc(id, 'scripts', WebScript.class);
        }
        return d;
    }

    public unload() {
        // Make sure it's hooked already before firing unload
        if (!Hooks.isHooked(_id as String)) return;
        Result r = Hooks.invoke('on script unload', [ 'id' : _id, 'hook' : hook, 'script' : this ]);
        // Unload any existing hooks
        Hooks.remove(_id);
    }

    public load() {

        if (contents == null)
            return;

        // First, unload if already loaded
        unload()

        GroovyShell compiler = new GroovyShell()

        // Server code is parsed first
        String html = (contents['server'] as String);

        // Utilizing layout?
        if (contents['layout'] != null && (contents['layout'] as String).trim().length() > 0)
            html +=
                '\ncontext.response.getWriter().println("""' +
                (contents['layout'] != null ? contents['layout'] as String : '')
                .replaceAll('(?i)<head>', '<head>')
                .replaceAll('(?i)</body>', '</body>') + '\n""")'

        // Design requires usage of layout (needs <head> tag to position itself)
        html = StringUtils.replace(html, '<head>',
                '<head><style>' + (contents['design'] != null ? contents['design'] + '</style>' : '<head>'))

        // As well as client-side (needs </body> tag to position itself)
        html = StringUtils.replace(html, '</body>',
                (contents['client'] != null ? contents['client'] : '') + '</body>')

        try {
            s = compiler.parse(Preprocessor.process(html as String));
        } catch (Exception e) {
            e.printStackTrace()
            StringWriter sw = new StringWriter()
            PrintWriter pw = new PrintWriter(sw)
            e.printStackTrace(pw)
            Result r = Hooks.invoke('on script compile error', [ 'id' : _id, 'error' : e, 'stack_trace' : pw.toString() ])
            if (r.cancelled) return;
        }

        closure = {
            Result r ->
                s.setProperty('context', r.context)
                try { s.run() } catch (Exception e) {
                    StringWriter sw = new StringWriter()
                    PrintWriter pw = new PrintWriter(sw)
                    e.printStackTrace(pw)
                    try { r.context['response'].getWriter().println('<server-error>' + sw.toString() + '</server-error>') }
                    catch (Exception ex) { e.printStackTrace() }
                }
        }

        // Register new hook
        if (hook != null)
            Hooks.add(hook, _id, closure);

        // Hook for loading -- makes loading more hooks inside scripts easier
        Result r = Hooks.invoke('on script load', [ 'id' : _id, 'hook' : hook, 'script' : this ]);
    }

}
