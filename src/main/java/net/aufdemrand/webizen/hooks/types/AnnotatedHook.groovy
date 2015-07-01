package net.aufdemrand.webizen.hooks.types

import net.aufdemrand.webizen.hooks.Result
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Created by Jeremy on 6/9/2015.
 */
class AnnotatedHook extends HookType {


    // Hooks referenced in Hooks.class are WeakReferenced to avoid unnecessary memory
    public static staticHooks = []


    public static getStaticHooks() {

        // Rid ourselves of old hooks -- BE GONE!
        staticHooks.clear();

        // Scan the entire classpath
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(""))
                .setScanners(new MethodAnnotationsScanner()))

        // Easy way to get all annotated methods
        Set<Method> hooked =
                reflections.getMethodsAnnotatedWith(Hook.class);

        println(hooked)

        // Is it static?
        for (Method m in hooked)
            if (Modifier.isStatic(m.getModifiers())) {
                // It is! New hook time.
                AnnotatedHook annotatedHook = new AnnotatedHook(m.getAnnotation(Hook.class).value())
                annotatedHook.m = m;
                staticHooks.add(annotatedHook)
            }


        println(staticHooks)

    }



    //
    // Instanced
    //

    public AnnotatedHook(def hookString) {
        super(hookString)
    }

    // This is our method to run!
    Method m;

    @Override
    void buzz(def context) {

        m.invoke(null, context);

    }



}
