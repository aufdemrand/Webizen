package net.aufdemrand.webizen.database

import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.BufferedHttpEntity
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.codehaus.jackson.map.DeserializationConfig
import org.codehaus.jackson.map.ObjectMapper
import org.eclipse.jetty.util.URIUtil

import javax.servlet.http.HttpServletResponse

/**
 * The bridge between Webizen's database functions and CouchDB's database.
 */

class CouchHandler {

    static CouchHandler couch;

    // We need a Couch to relax at!
    CouchHandler(String address) {

        // All we need is an address!
        if (!address.endsWith('/'))
            address =+ '/';

        couch = this;
        this.address = address;
    }

    // Address is referenced by the various HTTP calls to the CouchDB instance
    static def address;

    //
    // We can do things with the database!
    //

    /**
     * Gets a Document from the Couch Database. Returns null if the document doesn't exist.
     */
    public <T> T getDoc(String id, String database, Class type)  {

        // URL to fetch information from CouchDB
        def url = URIUtil.encodePath(address + database + '/' + id)

        // Client for making HTTP GET requests
        CloseableHttpClient httpClient = HttpClients.createDefault()
        HttpGet httpGet = new HttpGet(url)
        CloseableHttpResponse response = null

        // Returned string
        def returned

        // Execute request
        try {
            response = httpClient.execute(httpGet)
            HttpEntity entity = response.getEntity()
            returned = IOUtils.toString(entity.getContent())
            EntityUtils.consume(entity)
            print('GET [' + response.getStatusLine().statusCode + '] -> ' + returned)
        } catch (Exception e) { return null } finally {
            if (response != null) try { response.close() } catch (IOException e) { e.printStackTrace() }
        }

        // ObjectMapper for Jackson Deserialization
        ObjectMapper objectMapper = new ObjectMapper()
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // Fetch document from the return string
        try {
            def doc = objectMapper.readValue(returned, type)
            if (doc._id == null) return null;
            else return doc
        } catch (Exception e) { e.printStackTrace(); return null }
    }

    /**
     * Gets a Document from the Couch Database. Returns null if the document doesn't exist.
     */
    public getDocAttachment(String attachment, String id, String database, OutputStream o)  {

        // URL to fetch information from CouchDB
        def url = URIUtil.encodePath(address + database + '/' + id + '/' + attachment)

        // Client for making HTTP GET requests
        CloseableHttpClient httpClient = HttpClients.createDefault()
        HttpGet httpGet = new HttpGet(url)
        CloseableHttpResponse response = null

        // Returned string
        def returned = [:]

        // Execute request
        try {
            response = httpClient.execute(httpGet)
            HttpEntity entity = response.getEntity()
            returned = [ 'content_type' : entity.getContentType().value,
                         'content_length' : entity.getContentLength() ]
            println('ATTACHMENT [' + response.getStatusLine().statusCode + '] -> ' + entity.getContentType().value + ' .. ' + entity.getContentLength())
            entity.writeTo(o)
            EntityUtils.consume(entity)
        } catch (Exception e) { e.printStackTrace() } finally {
            if (response != null) try { response.close() } catch (IOException e) { e.printStackTrace() }
        }

        return returned;
    }

    /**
     * Adds a new Document to the Couch Database. Returns the Operation outcome.
     */
    public Operation addDoc(String id, String database) {

        // URL to add doc to the database
        def url = URIUtil.encodePath(address + database + '/' + id)

        // Client for making HTTP PUT requests
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        // Needs a valid entity, send along an empty JSON object
        httpPut.setEntity(new StringEntity('{}'))
        CloseableHttpResponse response = null;

        // Returned string
        String returned;

        // Execute request
        try {
            response = httpClient.execute(httpPut);
            HttpEntity entity = response.getEntity();
            returned = IOUtils.toString(entity.getContent());
            print('ADD [' + response.getStatusLine().statusCode + '] -> ' + returned);
            EntityUtils.consume(entity);
        } catch (Exception e) { e.printStackTrace(); } finally {
            if (response != null) try { response.close(); } catch (IOException e) { e.printStackTrace(); }
        }

        // ObjectMapper for Jackson Deserialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(
                DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper.readValue(returned, Operation.class);
    }

    /**
     * Updates a Document in the Couch Database. Returns the Operation outcome.
     */
    public Operation updateDoc(def doc, String database) {

        // URL to update doc to the database
        def url = URIUtil.encodePath(address + database + '/' + doc._id)

        // ObjectMapper for Jackson Serialization of the Document and Deserialization of the response
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Client for making HTTP PUT requests
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        // Send along the document
        httpPut.setEntity(new StringEntity(objectMapper.writeValueAsString(doc)))
        CloseableHttpResponse response = null;

        // Returned string
        def returned;

        // Execute request
        try {
            response = httpClient.execute(httpPut);
            // System.out.println(response.getStatusLine());
            HttpEntity entity1 = response.getEntity();
            returned = IOUtils.toString(entity1.getContent());
            print('UPDATE [' + response.getStatusLine().statusCode + '] -> ' + returned);
            EntityUtils.consume(entity1);
        } catch (Exception e) { e.printStackTrace(); } finally {
            if (response != null) try { response.close(); } catch (IOException e) { e.printStackTrace(); }
        }

        // Get the outcome of the request
        Operation operation = objectMapper.readValue(returned, Operation.class);

        // Update revision on the Doc to reflect the change
        if (operation.ok == true)
            doc._rev = operation.rev

        return operation;
    }

    /**
     * Removes a Document from the Couch Database. Returns the Operation outcome.
     */
    public Operation removeDoc(def doc, String database) {

        // URL to remove document from the Database
        def url = URIUtil.encodePath(address + database + '/' + doc._id)

        // Client for making HTTP DELETE requests
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete((String)(url + '?rev=' + doc._rev));
        CloseableHttpResponse response = null;

        // Returned string
        def returned;

        // Execute request
        try {
            response = httpClient.execute(httpDelete);
            HttpEntity entity1 = response.getEntity();
            returned = IOUtils.toString(entity1.getContent());
            print('REMOVE [' + response.getStatusLine().statusCode + '] -> ' + returned);
            EntityUtils.consume(entity1);
        } catch (Exception e) { e.printStackTrace(); } finally {
            if (response != null) try { response.close(); } catch (IOException e) { e.printStackTrace(); }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Get the outcome of the request
        Operation operation = objectMapper.readValue(returned, Operation.class);

        // Update revision on the Doc to reflect the change
        if (operation.ok == true)
            doc._rev = operation.rev

        return operation;
    }

    /**
     * Gets a View from the Couch Database. Returns null if the view doesn't exist.
     */
    public View getView(String document_id, String view_id, String database, String parameters)  {

        // URL to fetch information from CouchDB
        def url = URIUtil.encodePath(address + database + '/_design/' + document_id + '/_view/' + view_id) + '?' + parameters

        // Client for making HTTP GET requests
        CloseableHttpClient httpClient = HttpClients.createDefault()
        HttpGet httpGet = new HttpGet(url)
        CloseableHttpResponse response = null

        // Returned string
        def returned

        // Execute request
        try {
            response = httpClient.execute(httpGet)
            HttpEntity entity = response.getEntity()
            returned = IOUtils.toString(entity.getContent())
            EntityUtils.consume(entity)
            print('VIEW [' + response.getStatusLine().statusCode + '] -> ' + returned)
        } catch (Exception e) { return null } finally {
            if (response != null) try { response.close() } catch (IOException e) { e.printStackTrace() }
        }

        // ObjectMapper for Jackson Deserialization
        ObjectMapper objectMapper = new ObjectMapper()
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // Fetch document from the return string
        try {
            View view = new ObjectMapper().readValue(returned, View.class)
            view.database = database;
            return view
        } catch (Exception e) { return null }
    }


    /**
     * Gets all docs from the Couch Database. Returns null if the view doesn't exist.
     */
    public View getAll(String database, String parameters)  {

        // URL to fetch information from CouchDB
        def url = URIUtil.encodePath(address + database + '/_all_docs') + '?' + parameters

        // print(url)

        // Client for making HTTP GET requests
        CloseableHttpClient httpClient = HttpClients.createDefault()
        HttpGet httpGet = new HttpGet(url)
        CloseableHttpResponse response = null

        // Returned string
        def returned

        // Execute request
        try {
            response = httpClient.execute(httpGet)
            HttpEntity entity = response.getEntity()
            returned = IOUtils.toString(entity.getContent())
            EntityUtils.consume(entity)
            print('ALL_DOCS [' + response.getStatusLine().statusCode + '] -> ' + returned)
        } catch (Exception e) { return null } finally {
            if (response != null) try { response.close() } catch (IOException e) { e.printStackTrace() }
        }

        // ObjectMapper for Jackson Deserialization
        ObjectMapper objectMapper = new ObjectMapper()
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // Fetch document from the return string
        try {
            View view = new ObjectMapper().readValue(returned, View.class)
            view.database = database;
            return view
        } catch (Exception e) { return null }
    }


}
