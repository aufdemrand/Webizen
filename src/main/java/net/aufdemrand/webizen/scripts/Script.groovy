package net.aufdemrand.webizen.scripts

import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.database.Document
import net.aufdemrand.webizen.database.Operation
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import org.codehaus.jackson.annotate.JsonIgnore
import org.codehaus.jackson.map.ObjectMapper

/**
 * Created by Jeremy on 1/15/2015.
 */
abstract class Script {

    //
    // Static
    //

    public static Script get(def id) {
        Script d = CouchHandler.couch.getDoc(id, 'scripts', WebScript.class);
        if (d == null) {
            CouchHandler.couch.addDoc(id, 'scripts');
            d = CouchHandler.couch.getDoc(id, 'scripts', WebScript.class);
        }
        return d;
    }

    public static boolean exists(def id) {
        return CouchHandler.couch.getDoc(id, 'scripts', WebScript.class) != null;
    }

    public static List<Script> getAll() {
        return CouchHandler.couch.getAll('scripts', '').getAs(WebScript.class);
    }

    public static reloadAll() {
        for (Script s in getAll())
            try { s.load() } catch (Exception e) { e.printStackTrace() }
    }

    //
    // Instance
    //

    // Constructed upon load() to be executed when the given hook is called
    transient  Closure closure;

    // The compiled Groovy Script, constructed upon load()
    transient groovy.lang.Script s;

    // To be implemented by the specific script class for the type
    public abstract load();

    // Saves any changes to the Database
    public Operation save()   {
        Result r = Hooks.invoke('on script save', [ 'id' : _id ] );
        return CouchHandler.couch.updateDoc(this, 'scripts')
    }

    // Removes this record from the Database
    public Operation remove() { return CouchHandler.couch.removeDoc(this, 'scripts') }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // Serialized
    //

    // Documents have an ID and revision (managed by CouchDB)
    def _id, _rev

    // The name of the 'hook' that executes the script
    def hook

    // A small description of what the script does
    def description

    // Used in sorting similar scripts
    def group

    // The 'type' of script, used to indicate which Class of Script to use during loading
    def type

    // The contents of the script
    def contents = [:]

}
