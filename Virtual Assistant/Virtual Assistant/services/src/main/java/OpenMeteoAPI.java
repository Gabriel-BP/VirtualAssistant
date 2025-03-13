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

    // Coordenadas predeterminadas para Las Palmas de Gran Canaria
    private static final double DEFAULT_LATITUDE = 28.0997;
    private static final double DEFAULT_LONGITUDE = -15.4134;

    public OpenMeteoAPI(double latitude, double longitude) throws IOException {
        this.latitude = latitude;
        this.longitude = longitude;
        loadWeatherDescriptions();
    }

    public OpenMeteoAPI() throws IOException {
        // Constructor sin parámetros usa las coordenadas predeterminadas
        this(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
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

    public String fetchWeatherData(int requestType, String... params) throws IOException {
        // Si no se especifica un lugar, usar las coordenadas predeterminadas
        double requestLatitude = latitude;
        double requestLongitude = longitude;

        if (requestType == 1 && params.length >= 2) {
            // Si es una solicitud tipo 1 ("¿Qué tiempo hace en {lugar}?"), usar las coordenadas proporcionadas
            requestLatitude = Double.parseDouble(params[0]);
            requestLongitude = Double.parseDouble(params[1]);
        }

        String apiUrl = "";
        switch (requestType) {
            case 0: // ¿Qué tiempo hace?
                apiUrl = BASE_URL + "?latitude=" + requestLatitude + "&longitude=" + requestLongitude +
                        "&current=temperature_2m,weather_code&timezone=Europe%2FLondon";
                break;
            case 1: // ¿Qué tiempo hace en {lugar}?
                apiUrl = BASE_URL + "?latitude=" + requestLatitude + "&longitude=" + requestLongitude +
                        "&current=temperature_2m,weather_code&timezone=Europe%2FLondon";
                break;
            case 2: // ¿Cuál es la {variable}?
                if (params.length < 1) {
                    throw new IllegalArgumentException("Faltan parámetros para la solicitud tipo 2.");
                }
                apiUrl = BASE_URL + "?latitude=" + requestLatitude + "&longitude=" + requestLongitude +
                        "&current=" + params[0] + "&timezone=Europe%2FLondon";
                break;
            case 3: // ¿A qué hora es el atardecer hoy?
                apiUrl = BASE_URL + "?latitude=" + requestLatitude + "&longitude=" + requestLongitude +
                        "&daily=sunset&timezone=Europe%2FLondon";
                break;
            case 4: // ¿Lloverá pronto?
                apiUrl = BASE_URL + "?latitude=" + requestLatitude + "&longitude=" + requestLongitude +
                        "&hourly=temperature_2m,weather_code&timezone=Europe%2FLondon";
                break;
            default:
                throw new IllegalArgumentException("Tipo de solicitud no válido.");
        }

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

        return processResponse(response.toString(), requestType);
    }

    private String processResponse(String jsonResponse, int requestType) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        StringBuilder result = new StringBuilder();

        switch (requestType) {
            case 0: // ¿Qué tiempo hace?
            case 1: // ¿Qué tiempo hace en {lugar}?
                JSONObject current = jsonObject.getJSONObject("current");
                double temperature = current.getDouble("temperature_2m");
                int weatherCode = current.getInt("weather_code");
                String description = getWeatherDescription(weatherCode, isDayTime(current.getString("time")));
                result.append("Temperatura actual: ").append(temperature).append("°C, ")
                        .append(description);
                break;
            case 2: // ¿Cuál es la {variable}?
                current = jsonObject.getJSONObject("current");
                String variable = current.keys().next(); // Obtener la clave de la variable solicitada
                result.append("Valor actual: ").append(current.get(variable));
                break;
            case 3: // ¿A qué hora es el atardecer hoy?
                JSONArray daily = jsonObject.getJSONObject("daily").getJSONArray("sunset");
                result.append("Hora del atardecer: ").append(daily.getString(0));
                break;
            case 4: // ¿Lloverá pronto?
                JSONArray hourly = jsonObject.getJSONObject("hourly").getJSONArray("weather_code");
                boolean willRain = false;
                for (int i = 0; i < hourly.length(); i++) {
                    int code = hourly.getInt(i);
                    if (code >= 51 && code <= 67) { // Códigos relacionados con lluvia
                        willRain = true;
                        break;
                    }
                }
                result.append(willRain ? "Sí, lloverá pronto." : "No, no lloverá pronto.");
                break;
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