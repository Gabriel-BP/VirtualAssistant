import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("ALL")
public class TMDBService {
    private final String apiKey;

    public TMDBService(String apiKey) {
        this.apiKey = apiKey;
    }

    private String sendRequest(String endpoint) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3" + endpoint)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en la solicitud: " + response.code());
            }
            return response.body().string();
        }
    }

    // Películas
    public String getNowPlayingMovies() throws IOException {
        return sendRequest("/movie/now_playing?language=es-ES&page=1&region=ES");
    }

    public String getUpcomingMovies() throws IOException {
        return sendRequest("/movie/upcoming?language=es-ES&page=1&region=ES");
    }

    public String getPopularMovies() throws IOException {
        return sendRequest("/movie/popular?language=es-ES&page=1&region=ES");
    }

    // Series de TV
    public String getAiringTodayTV() throws IOException {
        return sendRequest("/tv/airing_today?language=es-ES&page=1&timezone=GMT");
    }

    public String getOnTheAirTV() throws IOException {
        return sendRequest("/tv/on_the_air?language=es-ES&page=1&timezone=GMT");
    }

    public String getPopularTV() throws IOException {
        return sendRequest("/tv/popular?language=es_ES&page=1");
    }

    // Géneros Películas
    public String getMovieGenres() throws IOException {
        return sendRequest("/genre/movie/list?language=es");
    }

    // Géneros Series
    public String getSeriesGenres() throws IOException {
        return sendRequest("/genre/tv/list?language=es");
    }

    public static void main(String[] args) {
        String configFilePath = "Virtual Assistant/utils/config.json";
        ConfigManager configManager = new ConfigManager(configFilePath);
        configManager.loadConfig();
        String apiKey = ConfigManager.getConfig("tmdb_api_key");
        TMDBService tmdb = new TMDBService(apiKey);

        try {
            //Obtener los códigos de los géneros de películas
            JSONObject codigos_generos_peliculas = new JSONObject(tmdb.getMovieGenres());
            //Guardarlo en utils
            try (FileWriter file = new FileWriter("Virtual Assistant/utils/movie_genres_codes.json")) {
                file.write(codigos_generos_peliculas.toString(2));
                System.out.println("JSON guardado correctamente en 'Virtual Assistant/utils/movie_genres_codes.json");
            } catch (IOException e) {
                System.out.println("Error al guardar el archivo: " + e.getMessage());
            }

            //Obtener los códigos de los géneros de series
            JSONObject codigos_generos_series = new JSONObject(tmdb.getSeriesGenres());
            //Guardarlo en utils
            try (FileWriter file = new FileWriter("Virtual Assistant/utils/tv_genres_codes.json")) {
                file.write(codigos_generos_series.toString(2));
                System.out.println("JSON guardado correctamente en 'Virtual Assistant/utils/tv_genres_codes.json");
            } catch (IOException e) {
                System.out.println("Error al guardar el archivo: " + e.getMessage());
            }

            // Obtener y almacenar respuestas JSON
            JSONObject en_cartelera = new JSONObject(tmdb.getNowPlayingMovies());
            // Formatear los datos según el formato requerido
            JSONObject formattedCurrentlyJson = TMDBFormatter.formatMovieData(en_cartelera);

            try (FileWriter file = new FileWriter("Virtual Assistant/utils/current_movies_formatted.json")) {
                file.write(formattedCurrentlyJson.toString(2));
                System.out.println("JSON guardado correctamente en 'Virtual Assistant/utils/current_movies_formatted.json'");
            } catch (IOException e) {
                System.out.println("Error al guardar el archivo: " + e.getMessage());
            }

            // También puedes obtener y formatear otros endpoints si los necesitas
            JSONObject proximamente = new JSONObject(tmdb.getUpcomingMovies());
            JSONObject formattedUpcoming = TMDBFormatter.formatMovieData(proximamente);

            try (FileWriter file = new FileWriter("Virtual Assistant/utils/upcoming_movies_formatted.json")) {
                file.write(formattedUpcoming.toString(2));
                System.out.println("JSON guardado correctamente en 'Virtual Assistant/utils/upcoming_movies_formatted.json'");
            } catch (IOException e) {
                System.out.println("Error al guardar el archivo: " + e.getMessage());
            }

            JSONObject popular_movies = new JSONObject(tmdb.getPopularMovies());
            JSONObject formattedPopular_movies = TMDBFormatter.formatMovieData(popular_movies);

            try (FileWriter file = new FileWriter("Virtual Assistant/utils/popular_movies_formatted.json")) {
                file.write(formattedPopular_movies.toString(2));
                System.out.println("JSON guardado correctamente en 'Virtual Assistant/utils/popular_movies_formatted.json'");
            } catch (IOException e) {
                System.out.println("Error al guardar el archivo: " + e.getMessage());
            }

            JSONObject airing_today_tv = new JSONObject(tmdb.getAiringTodayTV());
            JSONObject formattedAuring_tv = TMDBFormatter.formatMovieData(airing_today_tv);

            try (FileWriter file = new FileWriter("Virtual Assistant/utils/airing_today_tv_formatted.json")) {
                file.write(formattedAuring_tv.toString(2));
                System.out.println("JSON guardado correctamente en 'Virtual Assistant/utils/airing_today_tv_formatted.json'");
            } catch (IOException e) {
                System.out.println("Error al guardar el archivo: " + e.getMessage());
            }

            JSONObject on_the_air_tv = new JSONObject(tmdb.getOnTheAirTV());
            JSONObject formattedOn_the_air_tv = TMDBFormatter.formatMovieData(on_the_air_tv);

            try (FileWriter file = new FileWriter("Virtual Assistant/utils/on_the_air_tv_formatted.json")) {
                file.write(formattedOn_the_air_tv.toString(2));
                System.out.println("JSON guardado correctamente en 'Virtual Assistant/utils/on_the_air_tv_formatted.json'");
            } catch (IOException e) {
                System.out.println("Error al guardar el archivo: " + e.getMessage());
            }

            JSONObject popular_tv = new JSONObject(tmdb.getPopularTV());
            JSONObject formattedPopular_tv = TMDBFormatter.formatMovieData(popular_movies);

            try (FileWriter file = new FileWriter("Virtual Assistant/utils/popular_tv_formatted.json")) {
                file.write(formattedPopular_tv.toString(2));
                System.out.println("JSON guardado correctamente en 'Virtual Assistant/utils/popular_tv_formatted.json'");
            } catch (IOException e) {
                System.out.println("Error al guardar el archivo: " + e.getMessage());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}