package fr.vuzi.http;

import com.google.gson.Gson;

import fr.vuzi.file.FileChunk;
import fr.vuzi.file.FileMetadata;
import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;
import fr.vuzi.http.service.IHttpService;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class HttpServiceFile implements IHttpService {

    // Test
    // TODO: Use a database ? :)
    private Set<FileMetadata> files = new HashSet<>();
    private Map<String, FileChunk> chunks = new HashMap<>();

    private static Logger logger = Logger.getLogger(HttpServiceFile.class.getCanonicalName());

    public HttpServiceFile(Map<String, String> parameters) throws IOException {}

    private static final int CHUNCK_SIZE = 4096;

    @Override
    public void serve(IHttpRequest request, IHttpResponse response) throws HttpException {
        // TODO serve according to method :)

        logger.info("location => " + request.getLocation());
        logger.info("method => " + request.getMethod());
        for(Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            logger.info("header => " + header.getKey() + ":" + header.getValue());
        }

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
            chunk.size = size > CHUNCK_SIZE ? CHUNCK_SIZE : size;

            chunkList.add(chunk);
            size -= CHUNCK_SIZE;
        }

        fm.chunks = chunkList.toArray(new FileChunk[chunkList.size()]);

        // Save the file
        fm.path = fm.path.replace('\\', '/');
        files.add(fm);

        // Return the created file (+chunks)
        response.setBody(new Gson().toJson(fm).getBytes());
    }
}
