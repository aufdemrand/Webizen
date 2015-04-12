package net.aufdemrand.webizen

import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.objects.Encryptor
import net.aufdemrand.webizen.objects.InlineObject
import net.aufdemrand.webizen.objects.Objects
import net.aufdemrand.webizen.scripts.WebScript
import net.aufdemrand.webizen.scripts.YamlScript
import net.aufdemrand.webizen.web.Server
import net.aufdemrand.webizen.scripts.Script
import org.apache.commons.io.FilenameUtils

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService;


class Webizen {

    public static main(String[] args) throws Exception {

        // Initialize the Database
        new CouchHandler('http://192.168.254.205:5984/')

        // Initialize Objects
        Objects.loadObjectDefinitions()
        Encryptor.init()

        // Initialize Scripts
        YamlScript.loadYamlScripts()
        WebScript.reloadAll()

        Runnable r = new Runnable() {
            @Override
            void run() {
                Path folder = Paths.get("C:\\Users\\Jeremy\\Documents\\shuttle\\src\\main\\resources\\mod");
                WatchService watchService = FileSystems.getDefault().newWatchService();
                folder.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                boolean valid = true;
                while (valid) {
                    WatchKey watchKey = watchService.take();
                    for (WatchEvent event : watchKey.pollEvents()) {
                        WatchEvent.Kind kind = event.kind();
                        if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                            String fileName = event.context().toString();
                            println("File Created:" + fileName);
                            //YamlScript.loadYamlScripts()
                        }

                        if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
                            String fileName = event.context().toString();
                            println("File deleted:" + fileName);
                            //YamlScript.loadYamlScripts()
                        }

                        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
                            String fileName = event.context().toString();
                            println("File modified:" + fileName);
                            if (FilenameUtils.getExtension(fileName) == 'yaml')
                                Objects.loadObjectDefinitions()
                                YamlScript.loadYamlScripts()
                        }	}
                    valid = watchKey.reset();
                }
            }
        }

        new Thread(r).start()

        // Start the Web Server
        Server web_server = new Server(10000, 'http://127.0.0.1')
        new Thread(web_server).start()
    }

}
