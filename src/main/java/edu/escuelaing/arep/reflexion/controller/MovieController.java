package edu.escuelaing.arep.reflexion.controller;

import java.io.IOException;

import com.google.gson.JsonObject;

import edu.escuelaing.arep.reflexion.components.Component;
import edu.escuelaing.arep.reflexion.components.GetMapping;
import edu.escuelaing.arep.reflexion.service.HttpClient;

/**
 * This class is a controller of the application.
 */
@Component
public class MovieController {

    /**
     * This method is used to get the movies from the API.
     * @param requestQuery is the query to get the movies.
     * @return a JsonObject with the movies.
     */
    @GetMapping("/movie")
    public static JsonObject getMovies(String requestQuery) {
        try {
            return HttpClient.get(requestQuery);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
