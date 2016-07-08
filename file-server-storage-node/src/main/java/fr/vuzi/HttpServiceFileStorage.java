package fr.vuzi;


import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;
import fr.vuzi.http.service.IHttpService;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

public class HttpServiceFileStorage implements IHttpService {

    private static Logger logger = Logger.getLogger(HttpServiceFileStorage.class.getCanonicalName());

    private File dir;

    public HttpServiceFileStorage(Map<String, String> parameters) throws Exception {
        String dirname = parameters.get("dir");

        if(dirname == null || dirname.isEmpty())
            throw new Exception("No directory specified");

        dir = new File(dirname);

        if(!dir.exists() || !dir.isDirectory())
            throw new Exception("Directory doe not exist");
        else if(!dir.canWrite())
            throw new Exception("Directory not writable");

        // TODO get proxy address
    }

    @Override
    public void serve(IHttpRequest request, IHttpResponse response) throws HttpException {
        switch (request.getMethod()) {
            case "GET":
                serveSendChunk(request, response);
                break;
            case "POST":
                serveCreateChunk(request, response);
                break;
            case "DELETE":
                serveDeleteChunk(request, response);
                break;
            default:
                throw new HttpException(405, "Method not allowed");
        }
    }

    private void serveCreateChunk(IHttpRequest request, IHttpResponse response) {
        // TODO
        // Get location
        // Create file from location
        // Upload from body content to the file
        // Compare sha1 of file and provided sha1
        // Send to the proxy the chunk creation notice
        // Return positive response
    }

    private void serveDeleteChunk(IHttpRequest request, IHttpResponse response) {
        // TODO
        // Get location
        // Delete file from location
        // Send to the proxy the chunk deletion notice
        // Return positive response
    }

    private void serveSendChunk(IHttpRequest request, IHttpResponse response) {
        // TODO
        // Get location
        // Get file from location
        // Write content to response body
        // Return positive response
    }
}
