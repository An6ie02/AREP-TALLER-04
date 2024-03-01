package edu.escuelaing.arep.reflexion;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

     HttpServer server;

    @Before
    public void setUp() {
        server = HttpServer.getInstance();
        server.setFile("target/classes/public");
    }

    @Test
    public void testGetHtml() {
        byte[] response = null;
        try {
            response = server.responseBody("text/html", server.uriFile + "/index.html");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        assertNotNull(response);
    }

    @Test
    public void testGetCss() {
        byte[] response = null;
        try {
            response = server.responseBody("text/css", server.uriFile + "/css/stile.css");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        assertNotNull(response);
    }

}
