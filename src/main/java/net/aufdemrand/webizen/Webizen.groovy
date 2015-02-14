package net.aufdemrand.webizen

import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.web.Server
import net.aufdemrand.webizen.scripts.Script;


class Webizen {

    public static main(String[] args) throws Exception {

        // Initialize the Database
        new CouchHandler('http://192.168.254.205:5984/');

        // Initialize Scripts
        Script.reloadAll();

        // Start the Web Server
        Server web_server = new Server(10000, 'http://127.0.0.1');
        new Thread(web_server).start();

    }

}
