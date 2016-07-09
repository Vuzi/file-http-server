package fr.vuzi;

import com.google.gson.Gson;
import fr.vuzi.file.FileChunk;
import fr.vuzi.file.FileMetadata;
import fr.vuzi.file.Utils;
import fr.vuzi.http.impl.HttpRequest;
import fr.vuzi.http.impl.HttpResponse;
import fr.vuzi.http.request.HttpUtils;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());

    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 8081;

    public static void main(String[] args) {
        if(args.length < 3) {
            logger.info("Usage : <action> <file> <path>");
        }

        logger.info("Action => " + args[0]);
        logger.info("File => " + args[1]);
        logger.info("Path => " + args[2]);

        switch (args[0].toUpperCase()) {
            case "UPLOAD":
                uploadFile(args[1], args[2]);
                break;
            default:
                logger.severe(String.format("Unknown action => %s, aborting...", args[0]));
                System.exit(1);
        }
    }

    public static void uploadFile(String path, String dest) {
        try {
            logger.info("Starting the metadata file creation");

            File f = new File(path);
            File fdest = new File(dest);

            if(!f.isFile())
                throw new FileNotFoundException();

            FileMetadata fm = new FileMetadata();
            fm.name = fdest.getName();
            fm.path = fdest.getPath();
            fm.size = f.length();
            fm.sha1 = Utils.createSha1String(f);

            logger.info("File creation send to http://" + SERVER_NAME + ":" + SERVER_PORT + "/meta/" + fdest.toString().replace('\\', '/'));

            // Prepare request
            HttpRequest request = new HttpRequest();
            request.setMethod("PUT");
            request.setLocation("/meta/" + fdest.toString().replace('\\', '/'));
            request.setBody(new Gson().toJson(fm).getBytes());
            request.getHeaders().put("Content-Length", request.getBody().length + "");

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

            // Get updated
            fm = new Gson().fromJson(new InputStreamReader(response.getBody()), FileMetadata.class);

            // Close connection
            socket.close();

            // Show result
            logger.info(fm.toString());

            // Send the chunk (for now one at the time)
            FileInputStream inputStream = new FileInputStream(f);
            for(FileChunk fc : fm.chunks) {
                logger.info("Chunk " + fc.id + " upload");

                byte[] chunk = new byte[(int)fc.size];

                if(inputStream.read(chunk) != fc.size)
                    throw new IOException("Could not read the file");

                // Send the chunk
                request = new HttpRequest();
                request.setMethod("POST");
                request.setLocation("/data/" + fc.id);
                request.setBody(chunk);
                request.getHeaders().put("Content-Length", chunk.length + "");
                request.getHeaders().put("Content-sha1", Utils.createSha1String(chunk));

                // Send request
                socket = new Socket(SERVER_NAME, SERVER_PORT);

                outputStream = socket.getOutputStream();
                HttpUtils.RequestSender.send(request, outputStream);

                // Prepare response
                response = new HttpResponse();

                // Read response
                HttpUtils.ResponseParser.parse(response, socket.getInputStream());

                logger.info("Response received");
                logger.info("Code => " + response.getStatus() + " (" + response.getTextStatus() + ")");

                // Close connection
                socket.close();

                logger.info("Chunk " + fc.id + " uploaded successfully");
            }







            /*
            URL url = new URL("http://localhost:8081/meta/test/example");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestProperty("Connection","Close");
            con.setRequestMethod("PUT");

            con.setDoOutput(true);

            // Write body
            OutputStream outputStream = con.getOutputStream();
            outputStream.write(new Gson().toJson(fm).getBytes());
            outputStream.close();

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());

            FileInputStream inputStream = new FileInputStream(new File(path));
            byte[] buffer = new byte[1024];
            int read;

            while((read = inputStream.read(buffer)) > 0) {

            }*/


        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "File not found", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO Exception", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception", e);
        }
    }

}
