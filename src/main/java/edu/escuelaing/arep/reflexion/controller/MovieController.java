package edu.escuelaing.arep.reflexion.controller;

import java.io.IOException;

import com.google.gson.JsonObject;

import edu.escuelaing.arep.reflexion.components.Component;
import edu.escuelaing.arep.reflexion.components.GetMapping;
import edu.escuelaing.arep.reflexion.service.HttpClient;

@Component
public class MovieController {

    @GetMapping("/movie")
    public static JsonObject getMovies(String requestQuery) {
        try {
            System.out.println("Requesting movies...");
            System.out.println(requestQuery);
            return HttpClient.get(requestQuery);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
