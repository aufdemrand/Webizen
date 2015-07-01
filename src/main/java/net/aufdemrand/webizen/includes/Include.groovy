package net.aufdemrand.webizen.includes

import net.aufdemrand.webizen.Webizen
import net.aufdemrand.webizen.hooks.types.Hook
import org.apache.commons.io.FilenameUtils

import java.lang.reflect.Method
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

class Include {

    //
    // Static
    //

    static GroovyClassLoader classLoader;

    def classes = [:]

    public static initialize() {

        // Initial scan to load includes
        scan()

        // Watch for filesystem changes and scan() when necessary
        Runnable r = new Runnable() {
            @Override
            void run() {
                Path folder = Paths.get(Webizen.include_path);
                WatchService watchService = FileSystems.getDefault().newWatchService();
                folder.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                boolean valid = true;
                while (valid) {
                    WatchKey watchKey = watchService.take();
                    for (WatchEvent event : watchKey.pollEvents()) {
                        if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                            String fileName = event.context().toString();
                            println("File Created:" + fileName);
                        }

                        if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
                            String fileName = event.context().toString();
                            println("File deleted:" + fileName);
                        }

                        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
                            String fileName = event.context().toString();
                            println("File modified:" + fileName);
                            Include.scan()
                        }
                    }

                    valid = watchKey.reset();
                }
            }
        }

        // Watch in new Thread
        new Thread(r).start()
    }



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
