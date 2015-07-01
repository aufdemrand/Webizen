
package net.aufdemrand.webizen.hooks.types

import net.aufdemrand.webizen.hooks.Hooks

/**
 * Created by Jeremy on 6/4/2015.
 */

abstract class HookType {

    public def hookString;

    public abstract void buzz(def context);

    public HookType(def hookString) {
        // Note the hookString
        this.hookString = hookString
        // Notify Hooks of this Hook
        Hooks.notifyOf(this)
    }

}
