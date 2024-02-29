package edu.escuelaing.arep.reflexion;

public class App {

    public static void main(String[] args) {
        
        MDANSpark.getInstance().fileStatic("target/classes/public");

        try {
            MySpring.getInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);            
        }

        try {
            if (!HttpServer.getInstance().isRunning()) {
                HttpServer.getInstance().runServer();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
