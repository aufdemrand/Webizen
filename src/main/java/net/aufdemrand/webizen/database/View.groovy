package net.aufdemrand.webizen.database

import org.codehaus.jackson.annotate.JsonIgnore


class View {

    //
    // Static
    //

    public static View getView(String document_id, String view_id, String database) {
        return CouchHandler.couch.getView(document_id, view_id, database, '')
    }

    public static View getView(String document_id, String view_id, String database, String parameters) {
        return CouchHandler.couch.getView(document_id, view_id, database, parameters)
    }

    //
    // Instance
    //

    @JsonIgnore
    def database;

    public List<Document> getDocuments() {
        List<Document> docs = new ArrayList<Document>();
        for (def entry in rows)
            docs.add(Document.get(entry['id'], database))
        return docs;
    }

    public <T> T getAs(Class type) {
        def docs = new ArrayList<>();
        for (def entry in rows)
            docs.add(type.cast(CouchHandler.couch.getDoc(entry['id'], database, type)))
        return docs;
    }

    //
    // Serialized
    //

    def total_rows, rows, offset;

}
