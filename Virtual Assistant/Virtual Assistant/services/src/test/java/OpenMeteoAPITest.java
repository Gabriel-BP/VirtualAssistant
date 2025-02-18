import java.io.IOException;

public class OpenMeteoAPITest {
    public static void main(String[] args) {
        OpenMeteoAPI api = new OpenMeteoAPI();
        try {
            String response = api.fetchWeatherData();
            System.out.println(response);
        } catch (IOException e) {
            System.err.println("Error fetching weather data: " + e.getMessage());
        }
    }
}
