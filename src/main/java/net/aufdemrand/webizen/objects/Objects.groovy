package net.aufdemrand.webizen.objects

import org.yaml.snakeyaml.Yaml


class Objects {

    // Store definitions for constructing/passing along
    static definitions = [:]

    // Scans all .yaml files inside resources and checks them for object definitions, then
    // sends meta to the proper initializers for the different object types.
    public static loadObjectDefinitions() {

        // Deinitialize before initializing to avoid duplication/interference.
        for (def meta in definitions)
            InlineObject.deinitialize(meta)

        // TODO: Change scope of this variable
        def myDirectoryPath = "C:\\Users\\Administrator\\Google Drive\\Modules\\dev"

        println 'Scanning for objects.'
        File dir = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                Yaml yaml = new Yaml()
                Map<String, Object> obj = yaml.load(child.getText())
                // Loop through definitions inside the YAML file.
                if (obj != null)
                for (def definition in obj.keySet()) {
                    try {
                        // Store in definitions if of the appropriate types
                        if (obj[definition]['type'] == 'database-object'
                                || obj[definition]['type'] == 'inline-object') {
                            println '--> New object found: ' + obj[definition]['prefix']
                            definitions[obj[definition]['prefix']] = obj[definition];
                        }
                        // Run the returned meta through the initializer to do any
                        // pre-compilation or whatever.
                        if (obj[definition]['type'] == 'inline-object')
                            InlineObject.initialize(obj[definition]);
                    } catch (Exception e) { e.printStackTrace() }
                }
            }

        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }

        println 'All done!';

    }

    // Need a new object? Just give the type and args and this will get you one.
    public def static newObject(String id, Map args, def context) {
        def meta = definitions[id];
        if (meta["type"] == 'inline-object')
            return new InlineObject(meta, args, context);
    }

}
