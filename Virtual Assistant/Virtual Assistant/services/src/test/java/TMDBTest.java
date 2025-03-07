import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class TMDBTest {
    public static void main(String[] args) {
        String apiKey = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhMGRiNDFhYWY3MDZmZDJhYTBlODkwZTFjMGI1Njk1NCIsIm5iZiI6MTc0MTI1MzU1Ni41MDQsInN1YiI6IjY3Yzk2YmI0NGFmOGE2ODlhMjAzNDIwYyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.E8olcbeD9DPFranJaxIaPncZLa7rjF3Z55fdFThAG1E"; // Usa una API Key de TMDB v4 (Bearer Token)
        TMDBService tmdb = new TMDBService(apiKey);

        try {
            // Obtener y almacenar respuestas JSON
            // JSONObject en_cartelera = new JSONObject(tmdb.getNowPlayingMovies());
            // JSONObject proximamente = new JSONObject(tmdb.getUpcomingMovies());
            // JSONObject populares = new JSONObject(tmdb.getPopularMovies());

            // JSONObject tv_hoy = new JSONObject(tmdb.getAiringTodayTV());
            // JSONObject tv_aire = new JSONObject(tmdb.getOnTheAirTV());
             JSONObject tv_popular = new JSONObject(tmdb.getPopularTV());

            // Mostrar resultados
            printKeys(tv_popular, "");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printKeyMovie(JSONObject json, String key) {
        if (json.has("results")) {  // Accedemos al array de películas
            JSONArray results = json.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject movie = results.getJSONObject(i);

                if (movie.has(key)) { // Si la película tiene la key solicitada
                    System.out.println(movie.get(key)); // Imprimimos el valor (puede ser String, Integer, etc.)
                }
            }
        }
    }

    public static void printKeys(JSONObject json, String prefix) {
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            System.out.println(prefix + key);

            // Si el valor es otro JSONObject, se llama recursivamente
            if (value instanceof JSONObject) {
                printKeys((JSONObject) value, prefix + key + ".");
            }
            // Si es un JSONArray, iteramos sobre los elementos
            else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                if (array.length() > 0 && array.get(0) instanceof JSONObject) {
                    printKeys(array.getJSONObject(0), prefix + key + "[].");
                }
            }
        }
    }

}
