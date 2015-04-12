package net.aufdemrand.webizen.objects

import net.aufdemrand.webizen.database.Document

/**
 * Created by Jeremy on 3/22/2015.
 */
class DatabaseObject {

    GroovyShell compiler;

    def meta = [:]
    Map<String,Script> functions = [:]
    Document doc;

    public def call(String function_id) {
        return call(function_id, [:])
    }

    public def call(String function_id, def args) {
        functions[function_id].setProperty('args', args);
        return functions[function_id].run();
    }

    public DatabaseObject(def meta, def id, def db) {
        this.meta = meta;
        this.doc = Document.get(id, db)
        compileFunctions();
        if (functions.containsKey('load')) {
            functions['load'].setProperty('doc', doc)
            functions['load'].run();
        }
    }

    private compileFunctions() {
        GroovyShell compiler = new GroovyShell()
        compiler.setProperty('obj', this)
        if (meta['function-meta'] != null) {
            for (def f in meta['function-meta'].entrySet()) {
                if (f.getValue()['type'] == 'groovy') {
                    try {
                        functions[f.getKey()] =
                                compiler.parse(f.getValue()['code'])
                    } catch (Exception e) {
                        println('Error compiling function ' + f.getKey())
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}
