import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class NominatimGeocodingService {

    public static void main(String[] args) {
        try {
            String locationName = "Las Palmas de Gran Canaria"; // Example location in Spanish
            LocationCoordinates coordinates = getCoordinates(locationName);
            System.out.println("Latitude: " + coordinates.getLatitude());
            System.out.println("Longitude: " + coordinates.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LocationCoordinates getCoordinates(String locationName) throws Exception {
        // URL for Nominatim API
        String urlString = "https://nominatim.openstreetmap.org/search?q="
                + locationName.replace(" ", "+")
                + "&format=json&limit=1";

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONArray jsonArray = new JSONArray(response.toString());
        if (jsonArray.length() > 0) {
            JSONObject location = jsonArray.getJSONObject(0);

            double latitude = location.getDouble("lat");
            double longitude = location.getDouble("lon");

            return new LocationCoordinates(latitude, longitude);
        } else {
            throw new Exception("No results found for location: " + locationName);
        }
    }
}

class LocationCoordinates {
    private double latitude;
    private double longitude;

    public LocationCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}