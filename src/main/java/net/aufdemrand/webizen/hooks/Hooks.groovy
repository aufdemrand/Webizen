package net.aufdemrand.webizen.hooks

import net.aufdemrand.webizen.client.Console
import net.aufdemrand.webizen.hooks.types.HookType

import java.lang.ref.WeakReference


class Hooks {

    //
    // Static
    //

    private static def registered = new ArrayList<WeakReference<HookType>>()


    public static void notifyOf(HookType hookHandler) {
        registered.add(new WeakReference<HookType>(hookHandler))
    }


    public static Result invoke(String hookString, def context) {

        Console.report([ 'group' : 'hooks',
                         'message' : 'A hook has been invoked.',
                         'hook-id' : hookString,
                         'context' : context])

        // The result contains infos passed along to the hook, and accepts information
        // about the outcome (or, 'result') of the hook code.
        def result;

        // Check result-type -- some events have special result classes to make
        // things easier and faster when interacting with the API via scripting
        if (context['result-type'] instanceof Class) {
            if (Result.class.isAssignableFrom(context['result-type'] as Class)) {
                result = (context['result-type'] as Class).getConstructor(Object.class).newInstance(context)
                // Not sure why newInstance isn't propogating context -- set manually for now
                result.context = context;
            }
        }

        // -- but default to standard Result if none specified.
        if (result == null)
            result = new Result(context);

        // Buzz the Hook to check for execution
        for (WeakReference<HookType> hook in registered) {
            if (hook.get() != null) {
                if (hook.get().hookString.toLowerCase() == hookString.toLowerCase()) hook.get().buzz(result)
            }
        }

        // Deprecated -- now uses Buzz interface
        //
        //        // Figure out which hooks to call.  Hooks may contain some modifiers
        //        // in the context passed used to identify the hook handler
        //
        //        // Default hook-type to 'cached-closure'
        //
        //        if (context.containsKey('hook-type')) {
        //
        //            // 'class-static' invokes a static class in an 'include' groovy class
        //            if (context['hook-type'] == 'class-static') {
        //
        //            }
        //
        //            // 'cached-closure' invokes a closure
        //            if (context['hook-type'] == 'cached-closure') {
        //                if (hooks.containsKey(hookString.toLowerCase()))
        //                    try {
        //                        for (Closure c in hooks[hookString.toLowerCase()].values())
        //                            try { c.call(result) } catch (Exception e) { e.printStackTrace() }
        //                    } catch (Exception e) { e.printStackTrace() }
        //            }
        //
        //        }
        //
        //        //                 Hooks.invoke('on include initialized',[
        //        // 'hook-specifier' further trims which hook is actually fired
        //        // 'hook-specifier' : m.class.getCanonicalName(),
        //        // 'hook-type' set to 'class-static' -- include initializations are static methods
        //        // 'hook-type' : 'class-static' ])


        // Return mutated result
        return result;
    }




    //
    // Deprecated
    //


    @Deprecated
    private static Map<String, Map<String, Closure>> hooks = new HashMap<String, Map<String, Closure>>();


    @Deprecated
    public static boolean isHooked(String id) {
        for (Map<String, Closure> hks in hooks.values()) {
            for( Iterator<Map.Entry<String, Closure>> it = hks.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Closure> entry = it.next()
                if(entry.getKey().equals(id)) {
                    // found the id, return true -- this id is already hooked
                    return true;
                }
            }
        }
        // nothing found?
        return false;
    }


    @Deprecated
    public static void add(String hook, String id, Closure closure) {
        if (hook == null || id == null || closure == null)
            return;

        if (hooks[hook.toLowerCase()] == null)
            hooks[hook.toLowerCase()] = new HashMap<String, Closure>();

        // Add hook to the list
        hooks[hook.toLowerCase()][id.toLowerCase()] = closure;

        Console.report([ 'group' : 'hooks',
                         'message' : 'A new hook added.',
                         'hook' : hook,
                         'id' : id ])

    }


    @Deprecated
    public static void remove(String id) {
        for (Map<String, Closure> hks in hooks.values()) {
            for( Iterator<Map.Entry<String, Closure>> it = hks.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Closure> entry = it.next()
                if(entry.getKey().equals(id)) {

                    Console.report([ 'group' : 'hooks',
                                     'message' : 'A hook has been removed.',
                                     'id' : id ])

                    it.remove()
                }
            }
        }
    }


}
