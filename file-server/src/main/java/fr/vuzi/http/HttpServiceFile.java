package fr.vuzi.http;

import com.google.gson.Gson;

import fr.vuzi.file.FileChunk;
import fr.vuzi.file.FileMetadata;
import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.proxy.HttpServiceProxy;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;
import fr.vuzi.http.service.IHttpService;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class HttpServiceFile extends HttpServiceProxy {

    // Tests
    // TODO: Use a database ? :)
    private Set<FileMetadata> files = new HashSet<>();
    private Map<String, FileChunk> chunks = new HashMap<>();

    private static Logger logger = Logger.getLogger(HttpServiceFile.class.getCanonicalName());

    public HttpServiceFile(Map<String, String> parameters) throws Exception {
        super(parameters);
    }

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
            default:
            throw new HttpException(405, "Method not allowed");
        }
    }

    private void deleteFile(IHttpRequest request, IHttpResponse response) {
        // Get the file metadata
        FileMetadata fm = new Gson().fromJson(new String(request.getBody()), FileMetadata.class);

        logger.info("FileCreation.name => " + fm.name);
        logger.info("FileCreation.path => " + fm.path);
        logger.info("FileCreation.size => " + fm.size);

        // TODO get file from database

        for(FileChunk fc : fm.chunks) {
            for(String storageNode : fc.storageNodes) {
                // TODO delete file on node
            }
            // TODO delete chunk
        }

        // TODO delete file
    }

    private void editFile(IHttpRequest request, IHttpResponse response) {
        // TODO
    }

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
        files.add(fm);

        // Return the created file (+chunks)
        response.setBody(new Gson().toJson(fm).getBytes());
    }
}
