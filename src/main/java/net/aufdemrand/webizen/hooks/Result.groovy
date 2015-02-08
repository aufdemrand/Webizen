package net.aufdemrand.webizen.hooks

/**
 * The mutable 'result' of a Hook Call. Each hook member is passed this Result which can contain
 * additional information and objects from the hook's instance to the call.
 */
class Result {

    public Result(def context) {
        this.context = context;
    }

    boolean cancelled = false

    def context = [:]

}
