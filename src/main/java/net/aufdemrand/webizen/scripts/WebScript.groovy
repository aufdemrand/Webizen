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

    public load() {

        if (contents == null)
            return;

        // Unload any existing hooks
        Hooks.remove(_id);

        GroovyShell compiler = new GroovyShell()

        String html = (contents['server'] as String);

        if (contents['layout'] != null && (contents['layout'] as String).trim().length() > 0)
            html +=
                '\ncontext.response.getWriter().println("""' +
                (contents['layout'] != null ? contents['layout'] as String : '')
                .replaceAll('(?i)<head>', '<head>')
                .replaceAll('(?i)</body>', '</body>') + '\n""")'

        html = StringUtils.replace(html, '<head>',
                '<head><style>' + (contents['design'] != null ? contents['design'] + '</style>' : '<head>'))

        html = StringUtils.replace(html, '</body>',
                (contents['client'] != null ? contents['client'] : '') + '</body>')

        try { s = compiler.parse(Preprocessor.process(html as String)); } catch (Exception e) { e.printStackTrace(); print html }

        closure = {
            Result r ->
                s.setProperty('context', r.context)
                try { s.run() } catch (Exception e) {
                    StringWriter sw = new StringWriter()
                    PrintWriter pw = new PrintWriter(sw)
                    e.printStackTrace(pw)
                    try { r.context['response'].getWriter().println('<server-error>' + sw.toString() + '</server-error>') } catch (Exception ex) { e.printStackTrace() }
                }
        }

        // Register new hook
        if (hook != null)
            Hooks.add(hook, _id, closure);
    }

}
