package net.aufdemrand.webizen.objects

import groovy.json.JsonBuilder
import groovy.json.StringEscapeUtils
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import org.grooscript.GrooScript

import javax.servlet.http.HttpServletResponse

/**
 * Inline-objects represent data types that can handle parsing and rendering themselves.
 * To use them as custom html-elements, include handler-meta, rendering-meta, and attribute-meta.
 * To use them in a database field, ensure the function 'write-out' contains data necessary to
 * rebuild the object when passed as 'data' arg to 'construct'.
 */
class InlineObject {

// Shell for parsing Groovy code
    static GroovyShell shell;

// Compiled Scripts for rendering and functions for each inline-object type
// ['prefix':['name':Script],...]
    static def renderings = new ConfigObject();
    static def functions = new ConfigObject();

    // Safely removes an inline-object from the system. Also automatically called
    // when reinitializing or initializing an inline-object to avoid cross-contamination
    // of functions and renderings
    public static deinitialize(def meta) {
        return;
        // Need to know the type of inline-object we're dealing with
        def prefix = meta['prefix'];
        // Deinitialize hooks for handlers
        if (meta['handler-meta'] != null) {
            Hooks.remove(prefix + '-handler');
        }
        // Deinitialize hooks for rendering
        // TODO
        // Remove renderings and functions from
        // static defs for inline-objects of this type
        // TODO
    }

// Do compiling of grooscript/javascript/groovy before any actual instances
// are created or called to speed up execution
    public static initialize(def meta) {
        // Load shell if necessary
        if (shell == null)
            shell = new GroovyShell();
        // Compile any grooscript, which is flattened into javascript
        meta = checkForGrooscript(meta);
        // Create .js for client-side handling of the inline-object
        compileClientHandlersFor(meta);
        // Compile renderings
        compileRenderingsFor(meta);
        // Compile functions
        compileFunctionsFor(meta);
        // Compile styles
        compileDefaultStylesFor(meta);
    }

    private static compileDefaultStylesFor(meta) {
        if (meta['style-meta'] != null) {
            // Note the prefix of the passed meta
            def prefix = meta['prefix']
            // Loop through each rendering entry and parse the code contained
            // to store the Script output for later.
            for (def f in meta['style-meta'].entrySet()) {
                String code = f.getValue()['code']
                // Note the name of the rendering
                def style_name = f.getKey();
                // Name of the hook
                def hook_name = prefix + '-' + style_name + '-style-handler'
                // Hook handler is what will be hooked
                def hook_handler = 'on /' + prefix + '/style/' + style_name + ' hit'
                // Now add hook to handle fetching the .js file via www.my-website.com/render/object-prefix/rendering-name
                Hooks.add(hook_handler, hook_name, {
                        // Following code is the result of a hook firing which will contains several objects
                        // such as the Request, Response, etc. from the HTTP transaction
                    Result r ->
                        HttpServletResponse response = r.context['response'];
                        response.getWriter().print(code)
                        // Send appropriate content type
                        response.setContentType("text/css")
                        // And we're done!
                });
            }
        }
    }

    private static compileClientHandlersFor(meta) {
        // Build the .js file that will handle the custom-element code for
        // the client-side representation of the inline-object contained.
        if (meta['handler-meta'] != null) {
            // Standard DOM callbacks for custom-elements
            def on_attached = '', on_created = '', on_detached = '',
                on_attribute_changed = '', on_attribute_removed = '',
                on_attribute_created = '', on_document_ready = ''

            // Grab code from the handler-meta for the callbacks.
            for (String handler in meta['handler-meta'].keySet()) {
                // Grab code from meta entry
                def code = meta['handler-meta'][handler]['code'];
                // Check type of entry, match with appropriate def
                if (handler == 'on-attached') on_attached = code
                if (handler == 'on-created') on_created = code
                if (handler == 'on-detached') on_detached = code
                if (handler == 'on-attribute-changed') on_attribute_changed = code
                if (handler == 'on-attribute-created')  on_attribute_created = code
                if (handler == 'on-attribute-removed') on_attribute_removed = code
                if (handler == 'on-document-ready') on_document_ready = code
            }

            // Define some javascript to initialize the custom object with the document
            def javascript = """
            // Automatically generated. Do not modify.
            document.registerElement('${meta['prefix']}',{prototype: Object.create(HTMLElement.prototype, {
            createdCallback: { value: function() { ${on_created} }},
            attachedCallback: { value: function() { ${on_attached} }},
            detachedCallback: { value: function() { ${on_detached} }},
            attributeChangedCallback: { value: function(name, previousValue, value) {
            if (previousValue == null) { ${on_attribute_created} } else if (value == null) { ${on_attribute_removed} } else { ${on_attribute_changed} } }},
            render: {
              value: function(mthd) {
                var tar = this;
                if (mthd == undefined) {
                    if (this.getState() == null || this.getState() == '')
                        mthd = 'standard';
                    else mthd = this.getState();
                    }
                var fdata = {};
                if (this.hasAttributes()) {
                     var attrs = this.attributes;
                     for(var i = attrs.length - 1; i >= 0; i--) {
                        fdata[attrs[i].name] = attrs[i].value;
                     }
                }
                // console.log(fdata);
                return \\\$.ajax({
                        url: '/${meta['prefix']}/render/' + mthd,
                        data: fdata,
                        type: 'POST'
                    }).done(function(data, status, server) {
                        var d = JSON.parse(server.responseText);
                        var arr = d['attributes'];
                        for(var key in arr) {
                            tar.setAttribute(key, arr[key]);
                        }
                        tar.innerHTML = d['value'];
                    }
                );
            }},
            getState: {
              value: function() {
                return this.getAttribute('state');
                }}
            })});
            document.addEventListener('DOMContentLoaded', function(){ ${on_document_ready} });
            """

            // Add hook to handle fetching the .js file via www.my-website.com/js/object-prefix.js
            Hooks.add('on /' + (meta['prefix'] as String) + '/code/ hit',
                    (meta['prefix'] as String) + '-handler', {
                Result r ->
                    HttpServletResponse response = r.context['response']
                    response.getWriter().print(javascript)
                    response.setContentType("text/javascript")
            });
        }
    }

    private static def checkForGrooscript(def meta) {
        // Check handler-meta for any grooscript that needs to be converted
        // to javascript.
        for (def entry in meta['handler-meta'].entrySet()) {
            def script = entry.getValue()
            if (script['type'] == 'grooscript') {
                // Grab the code
                def grooscript = script['code']
                // Replace the code in the meta as javascript
                try {
                    meta['handler-meta'][entry.getKey()]['code'] = GrooScript.convert(grooscript);
                } catch (Exception e) { e.printStackTrace() }
            }
        }
        // return the meta with the newly replaced javascript
        return meta;
    }

    private static compileRenderingsFor(def meta) {
        // Renderings are how the client sends and fetches changes
        if (meta['rendering-meta'] != null) {
            // Note the prefix of the passed meta
            def prefix = meta['prefix']
            // Loop through each rendering entry and parse the code contained
            // to store the Script output for later.
            for (def f in meta['rendering-meta'].entrySet()) {
                String code
                // If 'ghtml', pretend we're in a multi-line gString
                if (f.getValue()['type'] == 'ghtml') {
                    code = 'return """' + f.getValue()['code'] + '"""\n'
                }
                // Otherwise, type == 'groovy'
                else if (f.getValue()['type'] == 'groovy') {
                    code = f.getValue()['code']
                }
                // Parse the code
                try {
                    renderings[prefix][f.getKey()] = code;
                } catch (Exception e) {
                    println('Error compiling function ' + f.getKey())
                    e.printStackTrace()
                }
                // Note the name of the rendering
                def rendering_name = f.getKey();
                // Name of the hook
                def hook_name = prefix + '-' + rendering_name + '-handler'
                // Hook handler is what will be hooked
                def hook_handler = 'on /' + prefix + '/render/' + rendering_name + ' hit'
                // Now add hook to handle fetching the .js file via www.my-website.com/render/object-prefix/rendering-name
                Hooks.add(hook_handler, hook_name, {
                        // Following code is the result of a hook firing which will contains several objects
                        // such as the Request, Response, etc. from the HTTP transaction
                    Result r ->
                        // Populate a map copy with parameter map from the request
                        def n = r.context['request'].getParameterMap();
                        // New map, see below why
                        def m = [:]
                        for ( def i in n.keySet()) {
                            // Parameter values are ALWAYS an array, which is kind of a pain.
                            // Assume single values are just that -- single values.
                            if (n[i] instanceof Object[] && n[i].size() == 1) m[i] = n[i][0];
                            else m[i] = n[i];
                        }
                        // Grab a new inline-object instance, sending along the parameters from the request as the arguments.
                        def o = Objects.newObject(prefix, m);
                        // Must start a new GroovyShell to avoid theading issues :(
                        // TODO: Figure out a way to cache this shit and compile outside of the hook closure.
                        def shell = new GroovyShell();
                        Script rendering = shell.parse(renderings[prefix][rendering_name])
                        rendering.setProperty('context', r.context) // The context from the hook (advanced usage, probably)
                        rendering.setProperty('obj', o) // The inline-object instance
                        // Set attribute for 'state' of the object
                        o.attribute['state'] = rendering_name
                        def returned;
                        try { returned = rendering.run() } catch (Exception e) { e.printStackTrace() }
                        // We'll return some json with the result of the rendering, plus the attributes
                        // from the inline-object instance.
                        def result = [ value: returned,
                                       attributes: o.attribute ];
                        // Write to the response's Writer the result of the json map through the JsonBuilder for the
                        // client to interpret the results
                        HttpServletResponse response = r.context['response'];
                        response.getWriter().print(new JsonBuilder(result).toPrettyString())
                        // Send appropriate content type
                        response.setContentType("application/json")
                        // And we're done!
                });
            }
        }
    }

    private static compileFunctionsFor(def meta) {
        // Functions are methods as inline-scripts are to javabeans.
        if (meta['function-meta'] != null) {
            // Note the prefix of the meta to know what type of inline-object we're dealing with
            def prefix = meta['prefix']
            // Loop through the function-meta and parse the code to form a groovy Script
            for (def f in meta['function-meta'].entrySet()) {
                if (f.getValue()['type'] == 'groovy') {
                    try { functions[prefix][f.getKey()] = f.getValue()['code'] } catch (Exception e) {
                        println('Error compiling function ' + f.getKey())
                        e.printStackTrace()
                    }
                }
            }
        }
    }


//
// INSTANCED
//

    public InlineObject(def meta, args) {
        this.meta = meta
        this.prefix = meta['prefix']
        this.instanceShell = new GroovyShell();
        this.instanceShell.setVariable('obj', this);
        this.attribute['uuid'] = 'el-' + UUID.randomUUID().toString()
        this.jQuery = "\$(\"[uuid=\'${this.attribute['uuid']}\']\")"
        call('construct', args)
    }

    def jQuery;
    def meta = [:]
    def attribute = [:]
    def prefix;
    def instanceShell;

    public def call(String function_id) {
        return call(function_id, [:])
    }

    public def call(String function_id, def args) {
        // Must start a new GroovyShell to avoid theading issues :(
        // TODO: Figure out a way to cache this shit outside of the instance.
        Script f = instanceShell.parse(functions[prefix][function_id])
        f.setProperty('args', args);
        // f.setProperty('obj', this);
        def returned;
        try { returned = f.run() } catch (Exception e) { e.printStackTrace() }
        return returned;
    }

}
