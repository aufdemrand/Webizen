package net.aufdemrand.webizen.objects

import org.yaml.snakeyaml.Yaml


class Objects {
    static definitions = [:]
    static hooks = [:]

    public static loadObjectDefinitions() {

        for (def meta in InlineObject.loaded_meta)
            InlineObject.deinitialize(meta)

        InlineObject.loaded_meta.clear();

        def myDirectoryPath = 'C:\\Users\\Jeremy\\Documents\\shuttle\\src\\main\\resources\\mod'

        println 'Scanning for objects.'
        println 'Scanning for objects.'
        File dir = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                Yaml yaml = new Yaml()
                Map<String, Object> obj = yaml.load(child.getText())
                for (def definition in obj.keySet()) {
                    if (obj[definition]['type'] == 'database-object'
                            || obj[definition]['type'] == 'inline-object') {
                        println '--> New object found: ' + obj[definition]['prefix']
                        definitions[obj[definition]['prefix']] = obj[definition];
                    }

                    // Initializers for inline-objects
                    if (obj[definition]['type'] == 'inline-object')
                        InlineObject.initialize(obj[definition]);

                }
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }

    }

    public def static newObject(String id, Map args) {
        def meta = definitions[id];
        if (meta["type"] == 'inline-object')
            return new InlineObject(meta, args);
    }

    public static test() {
        def test_number = newObject('us-phone-number', ['data':'(555) 666.7777'])
        println(test_number.functions['write-out'].run());
    }

}
