import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;

public class OpenMeteoAPI {
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private double latitude;
    private double longitude;
    private JSONObject weatherDescriptions; // Para almacenar las descripciones

    public OpenMeteoAPI(double latitude, double longitude) throws IOException {
        this.latitude = latitude;
        this.longitude = longitude;
        loadWeatherDescriptions();
    }

    private void loadWeatherDescriptions() throws IOException {
        // Cargar el archivo JSON con las descripciones
        File file = new File("Virtual Assistant/utils/weather_descriptions.json");
        if (!file.exists()) {
            throw new IOException("El archivo weather_descriptions.json no existe.");
        }
        String content = new Scanner(file).useDelimiter("\\Z").next();
        weatherDescriptions = new JSONObject(content);
    }

    public String fetchWeatherData() throws IOException {
        String apiUrl = BASE_URL + "?latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,weather_code&timezone=Europe%2FLondon";
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed: HTTP error code " + conn.getResponseCode());
        }
        Scanner scanner = new Scanner(url.openStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();
        conn.disconnect();
        return extractUsefulInfo(response.toString());
    }

    private String extractUsefulInfo(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONObject hourly = jsonObject.getJSONObject("hourly");
        JSONArray times = hourly.getJSONArray("time");
        JSONArray temperatures = hourly.getJSONArray("temperature_2m");
        JSONArray weatherCodes = hourly.getJSONArray("weather_code");

        StringBuilder result = new StringBuilder();
        result.append("Hourly Forecast:\n");

        for (int i = 0; i < times.length(); i++) {
            String time = times.getString(i);
            double temperature = temperatures.getDouble(i);
            int weatherCode = weatherCodes.getInt(i);

            // Obtener la descripción del código meteorológico
            String description = getWeatherDescription(weatherCode, isDayTime(time));

            result.append(time).append(" - ")
                    .append(temperature).append("°C, ")
                    .append(description).append("\n");
        }

        return result.toString();
    }

    private String getWeatherDescription(int weatherCode, boolean isDay) {
        JSONObject codeObject = weatherDescriptions.optJSONObject(String.valueOf(weatherCode));
        if (codeObject == null) {
            return "Descripción no disponible";
        }

        JSONObject period = isDay ? codeObject.getJSONObject("day") : codeObject.getJSONObject("night");
        return period.getString("description");
    }

    private boolean isDayTime(String time) {
        // Supongamos que "día" es entre las 6:00 AM y las 6:00 PM
        int hour = Integer.parseInt(time.split("T")[1].split(":")[0]);
        return hour >= 6 && hour < 18;
    }
}