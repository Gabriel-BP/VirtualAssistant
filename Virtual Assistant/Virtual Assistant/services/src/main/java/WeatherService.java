public class WeatherService {
    public static void main(String[] args) {
        try {
            String locationName = args.length > 0 ? String.join(" ", args) : "Las Palmas de Gran Canaria";
            LocationCoordinates coordinates = NominatimGeocodingService.getCoordinates(locationName);
            OpenMeteoAPI weatherAPI = new OpenMeteoAPI(coordinates.getLatitude(), coordinates.getLongitude());
            String weatherData = weatherAPI.fetchWeatherData();
            System.out.println("Weather for: " + locationName);
            System.out.println(weatherData);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}