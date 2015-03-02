package net.aufdemrand.webizen.flags

class Flags {

    static def all = new ConfigObject()

    static boolean exists(String flag_name) {
        return all.containsValue(flag_name);
    }

    static get(String flag_name) {
        // split path into parts
        def path = flag_name.split("\\.");
        // start with all as root
        def val = all
        try {
            // get recursively deeper
            for (p in path) {
                if (val[p] == null)
                    val[p] = new ConfigObject()
                val = val[p];
            }
            // once through the parts of the path, return the final val
            return val;
        } catch (Exception e) { e.printStackTrace() }
    }

    static set(String flag_name, def value) {
        def i = flag_name.lastIndexOf('.')
        def j = i + 1
        if (i <= 0) { i = 0; j = 0 }
        def split = [ flag_name.substring(0, i), flag_name.substring(j) ]
        def blah = get(split[0])
        blah[split[1]] = value
    }

}
