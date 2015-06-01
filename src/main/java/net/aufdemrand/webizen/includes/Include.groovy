package net.aufdemrand.webizen.includes

import net.aufdemrand.webizen.Webizen
import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.database.Document
import net.aufdemrand.webizen.database.Operation
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import net.aufdemrand.webizen.objects.InlineObject
import org.apache.commons.io.FilenameUtils
import org.codehaus.jackson.annotate.JsonIgnore
import org.codehaus.jackson.map.ObjectMapper
import org.yaml.snakeyaml.Yaml

class Include {

    //
    // Static
    //

    static GroovyClassLoader classLoader;

    def classes = [:]

    public static scan() {

        if (classLoader == null) classLoader = new GroovyClassLoader();

        File dir = new File(Webizen.include_path);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {

                if (FilenameUtils.getExtension(child.getName()) == 'groovy') {

                    try {
                        classLoader.parseClass(child)
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
        }

        println classLoader.getLoadedClasses()

    }
}
