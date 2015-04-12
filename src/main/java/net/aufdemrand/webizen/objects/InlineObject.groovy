package net.aufdemrand.webizen.objects

import groovy.json.JsonBuilder
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result

/**
 * Created by Jeremy on 3/22/2015.
 */
class InlineObject {


    public static def loaded_meta = []

    static Map<String,Script> renderings = [:]


    public static deinitialize(def meta) {
        if (meta['handler-meta'] != null) {
            Hooks.remove((meta['prefix'] as String) + '-handler');
        }
    }


    public static initialize(def meta) {

        // For removal
        loaded_meta.add(meta)

        // Create hook for serving the javascript handler as defined in the
        // 'handler-meta'
        if (meta['handler-meta'] != null) {

            // Standard DOM callbacks
            def on_attached = ''
            def on_created = ''
            def on_detached = ''
            def on_attribute_changed = ''
            def on_attribute_removed = ''
            def on_attribute_created = ''
            def on_document_ready = ''

            def events_map = [:]

            // Fill if found in handler-meta
            for (String handler in meta['handler-meta'].keySet()) {
                if (handler == 'on-attached') {
                    on_attached = meta['handler-meta'][handler]['code']
                } else if (handler == 'on-created') {
                    on_created = meta['handler-meta'][handler]['code']
                } else if (handler == 'on-detached')  {
                    on_detached = meta['handler-meta'][handler]['code']
                } else if (handler == 'on-attribute-changed')  {
                    on_attribute_changed = meta['handler-meta'][handler]['code']
                } else if (handler == 'on-attribute-created')  {
                    on_attribute_created = meta['handler-meta'][handler]['code']
                } else if (handler == 'on-attribute-removed')  {
                    on_attribute_removed = meta['handler-meta'][handler]['code']
                } else if (handler == 'on-document-ready')  {
                    on_document_ready = meta['handler-meta'][handler]['code']
                }
            }

            // Then compile into some javascript to initialize the custom object with the documnent
            def javascript = """

            document.registerElement(
              '${meta['prefix']}',
              {
                prototype: Object.create(
                  HTMLElement.prototype, {
                    createdCallback: {
                      value: function() {
                      ${on_created}
                    }},
                    attachedCallback: {
                      value: function() {
                      ${on_attached}
                    }},
                    detachedCallback: {
                      value: function() {
                      ${on_detached}
                    }},
                    attributeChangedCallback: {
                      value: function(name, previousValue, value) {
                      if (previousValue == null) {
                        ${on_attribute_created}
                      } else if (value == null) {
                        ${on_attribute_removed}
                      } else {
                         ${on_attribute_changed}
                      }
                    }},
                    render: {
                      value: function(mthd) {
                        var tar = this;
                        // console.log(this);
                        if (mthd == undefined) mthd = 'standard';
                        var fdata = {};
                        if (this.hasAttributes()) {
                             var attrs = this.attributes;
                             for(var i = attrs.length - 1; i >= 0; i--) {
                                // console.log(attrs[i].name);
                                fdata[attrs[i].name] = attrs[i].value;
                             }
                        }
                        console.log(fdata);
                        \$.ajax({
                            url: '/render/${meta['prefix']}/' + mthd, data: fdata,
                            type: 'POST', complete:
                            function( data, code ) {
                                // console.log('new data? ' + data.responseText);
                                var d = JSON.parse(data.responseText);
                                tar.innerHTML = d['value'];
                                console.log(d['value']);
                                var arr = d['attributes'];
                                for(var key in arr) {
                                    tar.setAttribute(key, arr[key]);
                                }
                            }
                            });
                    }}
                })
              }
            );

            document.addEventListener('DOMContentLoaded', function(){
               ${on_document_ready}
            });

            """

            // println(javascript)

            // Add hook to handle fetching the .js file via
            // www.my-website.com/js/object-prefix.js
            Hooks.add('on /js/' + (meta['prefix'] as String) + '.js hit',
                    (meta['prefix'] as String) + '-handler', {
                        Result r ->
                            r.context['response'].getWriter().print(javascript)
                            r.context['response'].setContentType("text/javascript;charset=utf-8")
                    });
        }

        // println('rendering meta?')

        GroovyShell shell = new GroovyShell();

        if (meta['rendering-meta'] != null) {
            // println('rendering meta!')
            for (def f in meta['rendering-meta'].entrySet()) {
                if (f.getValue()['type'] == 'ghtml') {
                    try {
                        renderings[f.getKey()] =
                                shell.parse('return """' + f.getValue()['code'] + '"""\n')
                    } catch (Exception e) {
                        println('Error compiling function ' + f.getKey())
                        e.printStackTrace()
                    }
                }
                if (f.getValue()['type'] == 'groovy') {
                    try {
                        renderings[f.getKey()] =
                                shell.parse(f.getValue()['code'])
                    } catch (Exception e) {
                        println('Error compiling function ' + f.getKey())
                        e.printStackTrace()
                    }
                }

                def rendering = f.getKey();

                // Add hook to handle fetching the .js file via
                // www.my-website.com/js/object-prefix.js
                Hooks.add('on /render/' + (meta['prefix'] as String) + '/' + f.getKey() + ' hit',
                        meta['prefix'] + '-' + f.getKey() + '-handler', {
                    Result r ->
                        // println '.....' + rendering
                        // println(r.context['request'])
                        def n = r.context['request'].getParameterMap();
                        def m = [:]
                        for ( def i in n.keySet()) {
                            print(n[i] instanceof Object[]);
                            if (n[i] instanceof Object[] && n[i].size() == 1)
                                m[i] = n[i][0];
                            else m[i] = n[i];
                        }
                        // println(m);
                        def o = Objects.newObject(meta['prefix'], m);
                        // println(o.meta['prefix'])
                        renderings[rendering].setProperty('context', r.context)
                        renderings[rendering].setProperty('obj', o)
                        def json = [ value: renderings[rendering].run(),
                                attributes: o.attribute ];
                        r.context['response'].getWriter().print(new JsonBuilder(json).toPrettyString())
                        r.context['response'].setContentType("text/html;charset=utf-8")
                });

            }
        }

    }


    GroovyShell compiler;

    def meta = [:]
    def attribute = [:]
    Map<String,Script> functions = [:]

    public def call(String function_id) {
        return call(function_id, [:])
    }

    public def call(String function_id, def args) {
        functions[function_id].setProperty('args', args);
        return functions[function_id].run();
    }

    public InlineObject(def meta, args) {
        this.meta = meta;
        compileFunctions();
        if (functions.containsKey('construct')) {
            functions['construct'].setProperty('args', args)
            // print(meta['function-meta']['construct']['code'])
            functions['construct'].run();
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
