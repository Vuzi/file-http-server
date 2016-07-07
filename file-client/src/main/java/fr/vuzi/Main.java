package fr.vuzi;

import com.google.gson.Gson;
import fr.vuzi.file.FileMetadata;
import fr.vuzi.file.Utils;

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

    public static void uploadFile(String path, String dest) {
        try {
            File f = new File(path);
            File fdest = new File(dest);

            if(!f.isFile())
                throw new FileNotFoundException();

            FileMetadata fm = new FileMetadata();
            fm.name = fdest.getName();
            fm.path = fdest.getPath();
            fm.size = f.length();
            fm.sha1 = Utils.createSha1String(f);

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
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception", e);
        }
    }

}
