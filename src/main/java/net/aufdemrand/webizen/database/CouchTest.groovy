package net.aufdemrand.webizen.database

import net.aufdemrand.webizen.scripts.Script


class CouchTest {

    public static main(String[] args) {

        // Get a Couch
        CouchHandler couch = new CouchHandler('http://68.70.176.226:5984/');

        println Script.getAll().get(0).save().wasSuccessful()

    }

}
