import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;

public class OpenMeteoAPI {
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude=28.0997&longitude=-15.4134&hourly=temperature_2m,weather_code&timezone=Europe%2FLondon";

    public String fetchWeatherData() throws IOException {
        URL url = new URL(API_URL);
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
            result.append(times.getString(i)).append(" - ")
                    .append(temperatures.getDouble(i)).append("°C, ")
                    .append(weatherCodes.getInt(i)).append("\n");
        }

        return result.toString();
    }
}
