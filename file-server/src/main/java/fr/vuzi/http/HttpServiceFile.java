package fr.vuzi.http;

import com.google.gson.Gson;

import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;
import fr.vuzi.http.service.IHttpService;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class HttpServiceFile implements IHttpService {

    public static class FileCreation {
        public String name;
        public String path;
        public long size;
    }

    public static class FileCreationResponse {
        public boolean error;
        public String message;
        public String[] chuncks;
    }

    private static Logger logger = Logger.getLogger(HttpServiceFile.class.getCanonicalName());

    public HttpServiceFile(Map<String, String> parameters) throws IOException {}

    private static final int CHUNCK_SIZE = 4096;

    @Override
    public void serve(IHttpRequest request, IHttpResponse response) throws HttpException {
        logger.info("location => " + request.getLocation());
        logger.info("method => " + request.getMethod());
        for(Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            logger.info("header => " + header.getKey() + ":" + header.getValue());
        }

        FileCreation fc = new Gson().fromJson(new String(request.getBody()), FileCreation.class);
        logger.info("FileCreation.name => " + fc.name);
        logger.info("FileCreation.path => " + fc.path);
        logger.info("FileCreation.size => " + fc.size);
    }
}
