package fr.vuzi.http;


import fr.vuzi.file.Utils;
import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;
import fr.vuzi.http.service.IHttpService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * File storage node service. This will handle creation, deletion and upload of chunks of file. The
 * chunk size will not be controlled. If provided, a sha1 hash will test the chunk integrity. The chunk
 * name should always be a valid UUID
 */
public class HttpServiceFileStorage implements IHttpService {

    private static Logger logger = Logger.getLogger(HttpServiceFileStorage.class.getCanonicalName());

    private File dir;
    private Pattern uuidRegex = Pattern.compile("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
    private String metadataServer;

    public HttpServiceFileStorage(Map<String, String> parameters) throws Exception {
        String dirname = parameters.get("dir");

        if(dirname == null || dirname.isEmpty())
            throw new Exception("No directory specified");

        dir = new File(dirname);
        dir.mkdir();

        if(!dir.exists() || !dir.isDirectory())
            throw new Exception("Directory doe not exist");
        else if(!dir.canWrite())
            throw new Exception("Directory not writable");

        // Proxy address
        metadataServer = parameters.get("metadataServer");
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

    /**
     * Send the chunk file object from the request. The chunk name should be a valid UUID
     * @param request The HTTP request
     * @return The file
     * @throws HttpException
     */
    private File getChunkFile(IHttpRequest request) throws HttpException {
        // Get location (location should be an UUID)
        String location = request.getParameter("location");

        if(location == null || !uuidRegex.matcher(location).matches())
            throw new HttpException(404, "Chunk not found (invalid UUID)");

        return new File(dir, location);
    }

    private void serveCreateChunk(IHttpRequest request, IHttpResponse response) throws HttpException {
        logger.info("chunk creation -> " + request.getParameter("location"));

        // Create file from location
        File chunk = getChunkFile(request);

        try {
            if(!chunk.createNewFile())
                throw new HttpException(403, "Chunk creation failed (file already created)");
        } catch (IOException e) {
            throw new HttpException(403, "Chunk creation failed", e);
        }

        // Upload from body content to the file
        try {
            FileOutputStream outputStream = new FileOutputStream(chunk);
            outputStream.write(request.getBody());
            outputStream.close();
        } catch (IOException e) {
            throw new HttpException(500, "Chunk witting error", e);
        }

        // Compare sha1 of file and provided sha1
        String sha1 = request.getHeader("Content-sha1");
        if(sha1 != null) {
            try {
                if(!sha1.toLowerCase().equals(Utils.createSha1String(chunk).toLowerCase()))
                    throw new HttpException(400, "Chunk hash mismatch");
            } catch (Exception e) {
                throw new HttpException(500, "Chunk hash calculation exception", e);
            }
        }

        // Send to the proxy the chunk creation notice
        // TODO metadataServer

        // Return positive response
        response.setStatus(200);
    }

    private void serveDeleteChunk(IHttpRequest request, IHttpResponse response) throws HttpException {
        logger.info("chunk deletion -> " + request.getParameter("location"));

        // Get file from location
        File chunk = getChunkFile(request);

        if(!chunk.isFile())
            throw new HttpException(404, "Chunk deletion failed (file not found)");
        else if(!chunk.canWrite())
            throw new HttpException(403, "Chunk deletion failed (file access denied)");

        // Delete file from location
        try {
            if(!chunk.delete())
                throw new HttpException(500, "Chunk deletion failed");
        } catch (SecurityException e) {
            throw new HttpException(500, "Chunk deletion failed", e);
        }

        // Send to the proxy the chunk deletion notice
        // TODO metadataServer

        // Return positive response
        response.setStatus(200);
    }

    private void serveSendChunk(IHttpRequest request, IHttpResponse response) throws HttpException {
        logger.info("chunk get -> " + request.getParameter("location"));

        // Get file from location
        File chunk = getChunkFile(request);

        // Write content to response body
        try {
            response.setBody(new FileInputStream(chunk));
        } catch (IOException e) {
            throw new HttpException(500, "Chunk reading error", e);
        }

        // Return positive response
        response.setStatus(200);
    }
}
