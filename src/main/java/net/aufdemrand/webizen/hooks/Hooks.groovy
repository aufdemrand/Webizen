package net.aufdemrand.webizen.hooks

class Hooks {

    //
    // Static
    //

    private static Map<String, Map<String, Closure>> hooks = new HashMap<String, Map<String, Closure>>();

    public static void add(String hook, String id, Closure closure) {
        if (hook == null || id == null || closure == null)
            return;

        if (hooks[hook.toLowerCase()] == null)
            hooks[hook.toLowerCase()] = new HashMap<String, Closure>();

        // Add hook to the list
        hooks[hook.toLowerCase()][id.toLowerCase()] = closure;
    }

    public static void remove(String id) {
        for (Map<String, Closure> hks in hooks.values()) {
            for( Iterator<Map.Entry<String, Closure>> it = hks.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Closure> entry = it.next()
                if(entry.getKey().equals(id)) {
                    println 'SCRIPT REMOVE -> ' + id
                    it.remove()
                }
            }
        }
    }

    public static Result invoke(String id, def context) {
        println 'HOOK INVOKE -> ' + id
        Result result = new Result(context);
        if (hooks.containsKey(id.toLowerCase()))
            try {
                for (Closure c in hooks[id.toLowerCase()].values())
                    try { c.call(result) } catch (Exception e) { e.printStackTrace() }
            } catch (Exception e) { e.printStackTrace() }

        return result;
    }

}
