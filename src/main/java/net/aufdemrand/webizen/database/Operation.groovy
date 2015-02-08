package net.aufdemrand.webizen.database

import org.codehaus.jackson.JsonGenerationException
import org.codehaus.jackson.map.JsonMappingException
import org.codehaus.jackson.map.ObjectMapper


class Operation {

    //
    // Serialized
    //

    def id, ok, rev, error, reason;

    /**
     * Checks if the Operation was marked successful.

     * If successful, this Operation:
     * will include the 'id' and 'rev' of the document modified (if any)

     * If unsuccessful, this Operation:
     * will include the 'error' and 'reason' of the issue
     */
    public boolean wasSuccessful() { return (ok == true) }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
