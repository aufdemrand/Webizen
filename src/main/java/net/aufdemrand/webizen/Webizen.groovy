package net.aufdemrand.webizen

import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.hooks.types.AnnotatedHook
import net.aufdemrand.webizen.includes.Include
import net.aufdemrand.webizen.web.Encryptor
import net.aufdemrand.webizen.web.Server
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger


class Webizen {

    public static def include_path = "C:\\Users\\Jeremy\\Documents\\Shuttle\\Includes"
    public static def static_path  = "C:\\Users\\Jeremy\\Documents\\Shuttle\\Static"

    public static main(String[] args) throws Exception {

        // Initialize the Database
        new CouchHandler('http://68.70.176.226:5984/')

        // Initialize Objects
        // Objects.loadObjectDefinitions()

        BasicConfigurator.configure();
        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        for ( Logger logger : loggers ) {
            logger.setLevel(Level.INFO);
        }

        Encryptor.init()
        Include.initialize()
        AnnotatedHook.getStaticHooks()

        // Start the Web Server
        Server web_server = new Server(10000, 'http://127.0.0.1')
        new Thread(web_server).start()
    }

}
