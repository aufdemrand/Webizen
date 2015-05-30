package net.aufdemrand.webizen.database

import groovy.json.JsonBuilder
import org.codehaus.jackson.annotate.JsonIgnore
import org.codehaus.jackson.map.DeserializationConfig
import org.codehaus.jackson.map.ObjectMapper


class View {

    //
    // Static
    //

    public static View get(String document_id, String view_id, String database) {
        return CouchHandler.couch.getView(document_id, view_id, database, '')
    }

    public static View get(String document_id, String view_id, String database, String parameters) {
        return CouchHandler.couch.getView(document_id, view_id, database, parameters)
    }

    //
    // Instance
    //

    @JsonIgnore
    def database;

    public List<Document> getDocuments() {
        List<Document> docs = new ArrayList<Document>();
        for (def entry in rows) {
            def returned = entry['doc']
            if (returned == null) {
                docs.add(CouchHandler.couch.getDoc(entry['id'], database, Document.class))
            } else {
                // ObjectMapper for Jackson Deserialization
                ObjectMapper objectMapper = new ObjectMapper()
                objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // Fetch document from the return string
                try {
                    def doc = objectMapper.readValue(new JsonBuilder(returned).toPrettyString(), Document.class)
                    if (doc._id == null) continue;
                    else {
                        docs.add(doc)
                    }
                } catch (Exception e) {
                    e.printStackTrace(); return null
                }
            }
        }
        return docs;
    }

    public <T> T getAs(Class type) {
        def docs = new ArrayList<>();
        for (def entry in rows) {
            def returned = entry['doc']
            if (returned == null) {
                docs.add(type.cast(CouchHandler.couch.getDoc(entry['id'], database, type)))
            } else {
                // ObjectMapper for Jackson Deserialization
                ObjectMapper objectMapper = new ObjectMapper()
                objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // Fetch document from the return string
                try {
                    def doc = objectMapper.readValue(new JsonBuilder(returned).toPrettyString(), type)
                    if (doc._id == null) continue;
                    else {
                        docs.add(doc)
                    }
                } catch (Exception e) {
                    e.printStackTrace(); return null
                }
            }
        }
        return docs;
    }

    //
    // Serialized
    //

    def total_rows, rows, offset;

}
