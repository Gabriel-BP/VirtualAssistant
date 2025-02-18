import java.io.IOException;

public class OpenMeteoTest {
    public static void main(String[] args) {
        try {
            // Usar coordenadas predeterminadas (Las Palmas de Gran Canaria)
            OpenMeteoAPI apiDefault = new OpenMeteoAPI();

            // ¿Qué tiempo hace? (Usa coordenadas predeterminadas)
            System.out.println(apiDefault.fetchWeatherData(0));

            // ¿Qué tiempo hace en París? (Coordenadas específicas)
            String locationName = "Paris"; // Example location in Spanish
            LocationCoordinates coordinates = NominatimGeocodingService.getCoordinates(locationName);
            OpenMeteoAPI apiParis = new OpenMeteoAPI(coordinates.getLatitude(), coordinates.getLongitude());
            System.out.println(apiParis.fetchWeatherData(1, "48.8566", "2.3522"));

            // ¿Cuál es la temperatura actual? (Usa coordenadas predeterminadas)
            System.out.println(apiDefault.fetchWeatherData(2, "temperature_2m"));

            // ¿Lloverá pronto? (Usa coordenadas predeterminadas)
            System.out.println(apiDefault.fetchWeatherData(4));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}