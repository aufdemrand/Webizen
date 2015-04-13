package net.aufdemrand.webizen.scripts

import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils
import org.yaml.snakeyaml.Yaml


class YamlScript extends Script {


    static scripts = [:]

    public static loadYamlScripts() {

        for (YamlScript s in scripts.values())
            s.unload();

        def myDirectoryPath = 'C:\\Users\\Jeremy\\Documents\\shuttle\\src\\main\\resources\\mod'

        File dir = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                try {
                    Yaml yaml = new Yaml()
                    Map<String, Object> obj = yaml.load(child.getText())
                    for (def s in obj.keySet()) {
                        if (obj[s]['type'] == 'hook-script') {
                            YamlScript y = new YamlScript();
                            y.hook = obj[s]['hook']
                            y.description = obj[s]['description']
                            y.contents = obj[s]['contents']
                            y._id = s
                            y.load()
                            scripts[s] = y;
                        }
                    }
                } catch (Exception e) { /* Incompatible file */ }
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }

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


        String html = '';

        // Render first
        int counter = 1
        // print(contents.keySet())
        while(counter <= contents.values().size()) {
            for (def section in contents.values()) {
                if (section['type'] == null) continue;
                if (section['order'] == counter || section['order'] == null) {
                    // print section['code'];
                    def code = section['code']

                    // format code depending on type
                    if (section['type'] == 'ghtml') {
                        code = '\ncontext.response.getWriter().println("""' + code + '""")\n'
                    } else if (section['type'] == 'ghtml-snippet') {
                        // code = 'code + '""")\n'
                    } else if (section['type'] == 'gcss') {
                        code = '\n<style>' + code + '</style>\n'
                    } else if (section['type'] == 'javascript') {
                        code = '\n<script>' +
                                StringUtils.replace( StringUtils.replace(code, '$(', '\\$('), '$.', '\\$.') +'</script>\n'
                    }

                    // 'insert-after string' strategy, or just append to end
                    if (section['insert-after'] != null) {
                        html = StringUtils.replace(html, section['insert-after'],
                                section['insert-after'] + code)
                    } else if (section['insert-before'] != null) {
                        html = StringUtils.replace(html, section['insert-before'],
                                code + section['insert-before'])
                    } else {
                        html += code
                    }
                }
            }
            counter++;
        }
        // print html

        try {
            GroovyShell compiler = new GroovyShell()
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
                    Hooks.invoke('on script execute error', [ 'id' : _id, 'error' : e, 'stack_trace' : pw.toString() ])
                }
        }

        // Register new hook
        if (hook != null)
            Hooks.add(hook, _id, closure);

        // Hook for loading -- makes loading more hooks inside scripts easier
        Hooks.invoke('on script load', [ 'id' : _id, 'hook' : hook, 'script' : this ]);
        Hooks.invoke('on script ' + _id + ' load', [ 'id' : _id, 'hook' : hook, 'script' : this ]);
    }

}
