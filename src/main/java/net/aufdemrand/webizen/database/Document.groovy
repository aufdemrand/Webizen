package net.aufdemrand.webizen.database

import net.aufdemrand.webizen.hooks.Hooks
import net.aufdemrand.webizen.hooks.Result
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileItemFactory
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.FilenameUtils
import org.codehaus.jackson.map.ObjectMapper

import javax.servlet.http.HttpServletRequest


class Document {

    //
    // Static
    //

    public static Document get(def id, def database) {
        Document doc = CouchHandler.couch.getDoc(id, database, Document.class)
        if (doc == null) {
            CouchHandler.couch.addDoc(id, database);
            doc = CouchHandler.couch.getDoc(id, database, Document.class)
        }
        if (doc != null) doc.database = database
        return doc
    }

    public static getNew(def database) {
        return get(UUID.randomUUID().toString().replace('-',''), database)
    }

    public static Document getIfExists(def id, def database) {
        Document doc = (Document) CouchHandler.couch.getDoc(id, database, Document.class)
        if (doc != null) doc.database = database
        return doc
    }

    public static boolean exists(def id, def database) {
        return CouchHandler.couch.getDoc(id, database, Document.class) != null
    }

    //
    // Instance
    //

    transient public String database;

    public getAttachment(String attachment, OutputStream o) {
        return CouchHandler.couch.getDocAttachment(attachment, _id, database, o)
    }

    public void getAttachmentsFrom(HttpServletRequest request) {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {
            println('multipart' + request)
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                List uploads = upload.parseRequest(request);
                for(FileItem i in uploads) {
                    try {
                        if (i.getName() == null)
                            continue;
                        println(i.getFieldName() + ' -> ' + i.getName())
                        def data = Base64.getEncoder().encodeToString(i.get())
                        def name = FilenameUtils.getName(i.getName())
                        def type = i.getContentType() != null ? i.getContentType() : 'application/download';
                        _attachments[name] = ['content_type': type, 'data': data]
                    } catch (Exception e) { println(i) }
                }
                print(_attachments)
            } catch (FileUploadException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // Saves any changes to the Database
    public Operation save()   {
        Result r = Hooks.invoke('on document save', [ 'type' : type ])
        return CouchHandler.couch.updateDoc(this, database)
    }

    // Removes this record from the Database
    public Operation remove() { return CouchHandler.couch.removeDoc(this, database) }

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

    def _attachments = [:]

    // They have Maps, keyed by an identifier, of Strings and List<String>s, and can be manipulated
    // with HTTP requests easily
    def fields = [:]

    // Private fields, also keyed by an identified, can contain any Object, manipulated with groovy-scripts only
    def private_fields = [:]

    // Dates which contain a Map of Long in the format of 'currenttimemillis', keyed by an identifier
    def dates = [:]

    // Stats can contain Integers or Booleans in a Map keyed by an identifier
    def stats = [:]

    // ...and a type
    def type

}
