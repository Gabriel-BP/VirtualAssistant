import org.json.JSONArray;
import org.json.JSONObject;


public class TMDBFormatter {

    /**
     * Convierte los resultados de la API de TMDB al formato personalizado requerido
     * @param tmdbJson JSON original recibido de la API de TMDB
     * @return Un JSON con el formato personalizado
     */
    public static JSONObject formatMovieData(JSONObject tmdbJson) {
        JSONObject formattedJson = new JSONObject();

        if (tmdbJson.has("results")) {
            JSONArray results = tmdbJson.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject movie = results.getJSONObject(i);

                if (movie.has("title") | movie.has("name")) {
                    if (movie.has("title") ) {
                        String title = movie.getString("title");
                    }
                    // Crear objeto con las propiedades requeridas
                    JSONObject movieDetails = new JSONObject();

                    if (movie.has("overview")) {
                        movieDetails.put("overview", movie.getString("overview"));
                    }

                    if (movie.has("genre_ids")) {
                        movieDetails.put("genre_ids", movie.getJSONArray("genre_ids"));
                    }

                    if (movie.has("release_date")) {
                        movieDetails.put("release_date", movie.getString("release_date"));
                    }

                    if (movie.has("vote_average")) {
                        movieDetails.put("vote_average", movie.getDouble("vote_average"));
                    }

                    if (movie.has("id")) {
                        movieDetails.put("id", movie.getInt("id"));
                    }

                    if (movie.has("adult")) {
                        movieDetails.put("adult", movie.getBoolean("adult"));
                    }

                    if (movie.has("first_air_date")){
                        movieDetails.put("first_air_date", movie.getString("first_air_date"));
                    }

                    // Agregar la película al JSON principal
                    if (movie.has("name")) {
                        String name = movie.getString("name");
                        formattedJson.put(name, movieDetails);
                    }

                    if (movie.has("title")) {
                        String title = movie.getString("title");
                        formattedJson.put(title, movieDetails);
                    }
                }
            }
        }

        return formattedJson;
    }

    /**
     * Metodo para mostrar el JSON formateado como String
     * @param tmdbJson JSON original recibido de la API de TMDB
     * @return String con el JSON formateado
     */
    public static String formatMovieDataAsString(JSONObject tmdbJson) {
        JSONObject formatted = formatMovieData(tmdbJson);
        return formatted.toString(2); // Con indentación de 2 espacios
    }
}