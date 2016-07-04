package fr.vuzi;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());

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

    public static void uploadFile(String path, String dest) {
        try {
            FileCreation fc = new FileCreation();
            fc.name = "myFile.txt";
            fc.path = "/path/to/";
            fc.size = 10000;

            URL url = new URL("http://localhost:8081/test/exemple");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestProperty("Connection","Close");
            con.setRequestMethod("POST");

            con.setDoOutput(true);

            // Write body
            OutputStream outputStream = con.getOutputStream();
            System.out.println(new Gson().toJson(fc));
            System.out.println(Arrays.toString(new Gson().toJson(fc).getBytes()));
            outputStream.write(new Gson().toJson(fc).getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());

            /*
            FileInputStream inputStream = new FileInputStream(new File(path));
            byte[] buffer = new byte[1024];
            int read;

            while((read = inputStream.read(buffer)) > 0) {

            }*/


        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "File not found", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO Exception", e);
        }
    }

}
