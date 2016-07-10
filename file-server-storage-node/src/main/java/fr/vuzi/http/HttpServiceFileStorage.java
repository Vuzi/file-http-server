package fr.vuzi.http;

import fr.vuzi.file.Utils;
import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.impl.HttpRequest;
import fr.vuzi.http.impl.HttpResponse;
import fr.vuzi.http.request.HttpUtils;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;
import fr.vuzi.http.service.IHttpService;

import java.io.*;
import java.net.Socket;
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

    // File server address and port
    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 8081;

    // TODO get from conf ?
    // Local server name and port
    private static final String HOSTNAME = "localhost";
    private static final int HOSTNAME_PORT = 8081;

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
     * Send a notice to the file server
     * @param id The chunk id
     * @param method The method, either PUT or DELETE
     * @throws HttpException
     */
    private void sendNotice(String id, String method) throws HttpException {
        try {
            // Prepare request
            HttpRequest request = new HttpRequest();
            request.setMethod(method);
            request.setLocation("/meta/chunk/" + id);
            request.getHeaders().put("Hostname", HOSTNAME + ":" + HOSTNAME_PORT);
            request.getHeaders().put("Content-Length", "0");
            request.setBody(new byte[0]);

            // Send request
            Socket socket = new Socket(SERVER_NAME, SERVER_PORT);

            OutputStream outputStream = socket.getOutputStream();
            HttpUtils.RequestSender.send(request, outputStream);

            // Prepare response
            HttpResponse response = new HttpResponse();

            // Read response
            HttpUtils.ResponseParser.parse(response, socket.getInputStream());

            logger.info("Response received");
            logger.info("Code => " + response.getStatus() + " (" + response.getTextStatus() + ")");

            if (response.getStatus() != 200)
                throw new HttpException(500, "Failed to send a " + method + " notice (" + response.getStatus() + ")");
        } catch (IOException e) {
            throw new HttpException(500, "Failed to send a " + method + " deletion notice");
        }
    }

    /**
     * Send a creation notice
     * @param id The chunk id
     * @throws HttpException
     */
    private void sendCreationNotice(String id) throws HttpException {
        sendNotice(id, "PUT");
    }

    /**
     * Send a delete notice
     * @param id The chunk id
     * @throws HttpException
     */
    private void sendDeletionNotice(String id) throws HttpException {
        sendNotice(id, "DELETE");
    }

    /**
     * Send the chunk file object from the id. The chunk name should be a valid UUID
     * @param id The id
     * @return The file
     * @throws HttpException
     */
    private File getChunkFile(String id) throws HttpException {
        // Id should be an UUID
        if(id == null || !uuidRegex.matcher(id).matches())
            throw new HttpException(404, "Chunk not found (invalid UUID)");

        return new File(dir, id);
    }

    /**
     * Handle a chunk creation request
     * @param request The request
     * @param response The response
     * @throws HttpException
     */
    private void serveCreateChunk(IHttpRequest request, IHttpResponse response) throws HttpException {
        String id = request.getParameter("location");
        logger.info("chunk creation -> " + id);

        // Create file from location
        File chunk = getChunkFile(id);

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
        sendCreationNotice(id);

        // Return positive response
        response.setStatus(200);
    }

    /**
     * Handle a chunk deletion request
     * @param request The request
     * @param response The response
     * @throws HttpException
     */
    private void serveDeleteChunk(IHttpRequest request, IHttpResponse response) throws HttpException {
        String id = request.getParameter("location");
        logger.info("chunk creation -> " + id);

        // Get file from location
        File chunk = getChunkFile(id);

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

    /**
     * Handle a chunk request
     * @param request The request
     * @param response The response
     * @throws HttpException
     */
    private void serveSendChunk(IHttpRequest request, IHttpResponse response) throws HttpException {
        String id = request.getParameter("location");
        logger.info("chunk creation -> " + id);


        // Get file from location
        File chunk = getChunkFile(id);

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
