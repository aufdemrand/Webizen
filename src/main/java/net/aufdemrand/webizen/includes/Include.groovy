package net.aufdemrand.webizen.includes

import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.database.Document
import net.aufdemrand.webizen.database.Operation
import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import org.codehaus.jackson.annotate.JsonIgnore
import org.codehaus.jackson.map.ObjectMapper

class Include {

    //
    // Static
    //

    public static List<Include> getAll() {
        return CouchHandler.couch.getAll('includes', 'include_docs=true').getAs(Include.class);
    }

    public static Include get(def id) {
        Include d = CouchHandler.couch.getDoc(id, 'includes', Include.class);
        if (d == null) {
            CouchHandler.couch.addDoc(id, 'includes');
            d = CouchHandler.couch.getDoc(id, 'includes', Include.class);
        }
        return d;
    }

    //
    // Instance
    //

    // Saves any changes to the Database
    public Operation save()   {
        Result r = Hooks.invoke('on include save', [ 'name' : name ] );
        return CouchHandler.couch.updateDoc(this, 'includes')
    }

    // Removes this record from the Database
    public Operation remove() { return CouchHandler.couch.removeDoc(this, 'includes') }

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

    // The name of the include used to call it
    def name

    // Group
    def group, description

    // The contents of the include
    def contents

}
