import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

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
}