package edu.escuelaing.arep.reflexion;

import java.io.BufferedReader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;

import edu.escuelaing.arep.reflexion.service.Function;

public class HttpServer {

    public static String uriFile;
    private static HttpServer _instance;
    private boolean running = false;

    private HttpServer() {
    }

    public static HttpServer getInstance() {
        if (_instance == null) {
            _instance = new HttpServer();
        }
        return _instance;
    }

    /**
     * Main method that creates a socket server and initializes the connection with
     * the client.
     * 
     * @param args Arguments of the main method.
     * @throws IOException        If an input or output exception occurred.
     * @throws URISyntaxException
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     */
    public void runServer() throws IOException, URISyntaxException, IllegalAccessException, InvocationTargetException {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            boolean firstLine = true;
            String uriStr = "";
            String method = "";

            while ((inputLine = in.readLine()) != null) {
                if (firstLine) {
                    method = inputLine.split(" ")[0];
                    uriStr = inputLine.split(" ")[1];
                    firstLine = false;
                    System.out.println("Received: " + inputLine);
                }
                if (!in.ready()) {
                    break;
                }
            }

            URI requesUri = new URI(uriStr);
            byte[] content;
            byte[] outputLineBytes;
            String format = "text/plain";

            try {
                if (requesUri.getPath().startsWith("/action")) {
                    content = callService(requesUri, method);
                } else if (requesUri.getPath().startsWith("/component")) {
                    content = callComponent(requesUri, method);
                } else {
                    String path = uriFile + requesUri.getPath();
                    format = getFileFormat(path);
                    content = responseBody(format, path);
                }
            } catch (IOException e) {
                format = "text/html";
                content = responseBody("text/html", uriFile + "/error.html");
            }
            outputLineBytes = contentHeader(format);

            try (OutputStream os = clientSocket.getOutputStream()) {
                os.write(outputLineBytes);
                os.write(content);
            } catch (IOException e) {
                System.out.println("Error: enviando mensaje");
            }

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();

    }

    private byte[] callComponent(URI requestUri, String method) throws IllegalAccessException, InvocationTargetException {
        System.out.println("Calling component");
        System.out.println(requestUri);
        String componentUri = requestUri.getPath().substring(10);
        System.out.println(componentUri);
        Method service = MySpring.getMethod(componentUri, method);
        JsonObject response;
        response = (JsonObject) service.invoke(null, requestUri.getQuery());
        return response.toString().getBytes();
    }

    private byte[] callService(URI requestUri, String method) throws IOException {
        String serviceUri = requestUri.getPath().substring(7);
        Function service = MDANSpark.search(serviceUri, method);
        byte[] response = service.handle(requestUri.toString()).getBytes();
        return response;
    }

    /**
     * Method that returns the content of a file in bytes.
     * 
     * @param format ContentType of the File.
     * @param path   Path of the file.
     * @return Byte array with the content of the file.
     * @throws IOException
     * @throws URISyntaxException
     */
    public byte[] responseBody(String format, String path) throws IOException, URISyntaxException {
        URI uri = new URI(path);
        if (format.startsWith("image")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage image = ImageIO.read(new File(uri.getPath()));
            String formatName = format.split("/")[1];
            ImageIO.write(image, formatName, baos);
            return baos.toByteArray();
        }
        return Files.readAllBytes(Paths.get(path));
    }

    /**
     * Method that returns the header of the response.
     * 
     * @param type    ContentType of the response.
     * @param content Content of the response.
     * @return String with the header of the response.
     */
    public byte[] contentHeader(String type) {
        String responseH = "HTTP/1.1 200 OK\r\n"
                + "Accept-Ranges: bytes\r\n"
                + "Content-Type: " + type + "\r\n"
                + "\r\n";
        return responseH.getBytes();

    }

    /**
     * Method that returns the ContentType of a file.
     * 
     * @param uriStr URI of the request.
     * @return String with the ContentType of the file.
     */
    public static String getFileFormat(String uriStr) {
        String format = "";
        if (uriStr.endsWith(".html")) {
            format = "text/html";
        } else if (uriStr.endsWith(".css")) {
            format = "text/css";
        } else if (uriStr.endsWith(".js")) {
            format = "application/javascript";
        } else if (uriStr.endsWith(".png")) {
            format = "image/png";
        } else {
            format = "text/plain";
        }
        return format;
    }

    public void setFile(String path) {
        uriFile = path;
    }

    public boolean isRunning() {
        return running;
    }

}
