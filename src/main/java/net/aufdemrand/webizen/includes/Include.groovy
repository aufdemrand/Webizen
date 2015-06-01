package net.aufdemrand.webizen.includes

import net.aufdemrand.webizen.Webizen
import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.database.Document
import net.aufdemrand.webizen.database.Operation
import net.aufdemrand.webizen.hooks.Hook
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import net.aufdemrand.webizen.objects.InlineObject
import org.apache.commons.io.FilenameUtils
import org.codehaus.jackson.annotate.JsonIgnore
import org.codehaus.jackson.map.ObjectMapper
import org.yaml.snakeyaml.Yaml

import java.lang.reflect.Method

class Include {

    //
    // Static
    //

    static GroovyClassLoader classLoader;

    def classes = [:]

    public static scan() {

        if (classLoader == null) {
            classLoader = new GroovyClassLoader()
            classLoader.addClasspath(Webizen.include_path)
        };

        // Clear the cache first
        classLoader.clearCache()

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

        // println classLoader.loadedClasses

        for (Class c in classLoader.loadedClasses)
            for (Method m in c.getMethods())
                if (m.isAnnotationPresent(Hook.class))
                    if (m.getAnnotation(Hook.class).value() == 'on include initialized')
                        m.invoke(null)

    }
}
