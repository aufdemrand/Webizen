package net.aufdemrand.webizen.web

import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.eclipse.jetty.util.URIUtil

class RequestCreator {

    public static String get(String url, Map options = null) {
        // URL to fetch information from CouchDB
        if (options?.get('encode') == true) url = URIUtil.encodePath(url)
        // Client for making HTTP GET requests
        CloseableHttpClient httpClient = HttpClients.createDefault()
        HttpGet httpGet = new HttpGet(url)
        CloseableHttpResponse response = null
        // Returned string
        def returned = ''
        // Execute request
        try {
            response = httpClient.execute(httpGet)
            HttpEntity entity = response.getEntity()
            returned = IOUtils.toString(entity.getContent())
            EntityUtils.consume(entity)
            // println 'GET [' + response.getStatusLine().statusCode + '] -> ' + url
        } catch (Exception e) { return null } finally {
            if (response != null) try { response.close() } catch (IOException e) { e.printStackTrace() }
        }
        return returned;
    }

    public static String post(String url, String body, Map params) {
        // URL to fetch information from CouchDB
        url = URIUtil.encodePath(url)
        // Client for making HTTP GET requests
        CloseableHttpClient httpClient = HttpClients.createDefault()
        HttpPost httpPost = new HttpPost(url)
        CloseableHttpResponse response = null
        MultipartEntity req_entity = new MultipartEntity()
        for (def en in params.entrySet())
            req_entity.addPart(en.getKey(), new StringBody(en.getValue()));
        httpPost.setEntity(req_entity)
        // Returned string
        def returned = ''
        // Execute request
        try {
            response = httpClient.execute(httpPost)
            HttpEntity entity = response.getEntity()
            returned = IOUtils.toString(entity.getContent())
            EntityUtils.consume(entity)
            // println('POST [' + response.getStatusLine().statusCode + '] -> ' + url)
        } catch (Exception e) { return null } finally {
            if (response != null) try { response.close() } catch (IOException e) { e.printStackTrace() }
        }
        return returned;
    }

}
