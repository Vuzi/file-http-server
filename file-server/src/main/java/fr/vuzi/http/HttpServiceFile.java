package fr.vuzi.http;

import com.google.gson.Gson;

import fr.vuzi.file.FileChunk;
import fr.vuzi.file.FileMetadata;
import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.proxy.HttpServiceProxy;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;
import fr.vuzi.http.service.IHttpService;

import java.util.*;
import java.util.logging.Logger;

public class HttpServiceFile implements IHttpService {

    // Tests
    // TODO: Use a database ? :)
    private static Map<String, FileMetadata> files = new HashMap<>();
    private static Map<String, FileChunk> chunks = new HashMap<>();

    private static Logger logger = Logger.getLogger(HttpServiceFile.class.getCanonicalName());

    public HttpServiceFile(Map<String, String> parameters) throws Exception {}

    private static final int CHUNK_SIZE = 4096;

    @Override
    public void serve(IHttpRequest request, IHttpResponse response) throws HttpException {
        logger.info("location => " + request.getLocation());
        logger.info("method => " + request.getMethod());
        for(Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            logger.info("header => " + header.getKey() + ":" + header.getValue());
        }

        // TODO: generic message and responses

        switch (request.getMethod()) {
            case "PUT":
                createFile(request, response);
                break;
            case "POST":
                editFile(request, response);
                break;
            case "DELETE":
                deleteFile(request, response);
                break;
            case "GET":
                getFile(request, response);
                break;
            default:
            throw new HttpException(405, "Method not allowed");
        }
    }

    /**
     * Return the metadata of a file
     * @param request The request
     * @param response The response
     * @throws HttpException
     */
    private void getFile(IHttpRequest request, IHttpResponse response) throws HttpException {
        // Get the file metadata
        // TODO get file from database
        FileMetadata fm = files.get(request.getParameter("location"));

        if(fm == null)
            throw new HttpException(404, "File not found");

        // Return the file (+chunks)
        response.setBody(new Gson().toJson(fm).getBytes());
        response.setStatus(200);
    }

    /**
     * Delete a file (data + metadata)
     * @param request The request
     * @param response The response
     * @throws HttpException
     */
    private void deleteFile(IHttpRequest request, IHttpResponse response) throws HttpException {
        // Get the file metadata
        // TODO get file from database
        FileMetadata fm = files.get(request.getParameter("location"));  //new Gson().fromJson(new String(request.getBody()), FileMetadata.class);

        if(fm == null)
            throw new HttpException(404, "File not found");

        logger.info("FileCreation.name => " + fm.name);
        logger.info("FileCreation.path => " + fm.path);
        logger.info("FileCreation.size => " + fm.size);

        for(FileChunk fc : fm.chunks) {
            for(String storageNode : fc.storageNodes) {
                // TODO delete file on node
            }
            chunks.remove(fc.id);
        }
        files.remove(fm.path + "/" + fm.name);

        // Return positive response
        response.setStatus(200);
    }

    private void editFile(IHttpRequest request, IHttpResponse response) {
        // TODO
    }

    /**
     * Create a file (only metadata)
     * @param request The request
     * @param response The response
     */
    private void createFile(IHttpRequest request, IHttpResponse response) {
        // Get the file metadata
        FileMetadata fm = new Gson().fromJson(new String(request.getBody()), FileMetadata.class);

        logger.info("FileCreation.name => " + fm.name);
        logger.info("FileCreation.path => " + fm.path);
        logger.info("FileCreation.size => " + fm.size);

        // TODO :
        // - check that file doesn't exist already
        // - check that file information is coherent

        // List of chunks of the file
        List<FileChunk> chunkList = new ArrayList<>();

        long size = fm.size;
        while(size > 0) {
            FileChunk chunk = new FileChunk();
            chunk.id = UUID.randomUUID().toString();
            chunk.size = size > CHUNK_SIZE ? CHUNK_SIZE : size;

            chunkList.add(chunk);
            size -= CHUNK_SIZE;
        }

        fm.chunks = chunkList.toArray(new FileChunk[chunkList.size()]);

        // Save the file
        fm.path = fm.path.replace('\\', '/');
        files.put(fm.path + "/" + fm.name, fm);

        // Return the created file (+chunks)
        response.setBody(new Gson().toJson(fm).getBytes());
        response.setStatus(200);
    }
}
