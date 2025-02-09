import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FootballService {

    private static final String API_KEY = "45d816d6a4msh3ce93b1797ab7f4p11b4dejsn93e51ce2e0fc";
    private static final String BASE_URL = "https://api-football-v1.p.rapidapi.com/v2";
    private static final String YEAR = java.time.LocalDate.now().toString().split("-")[0];

    public void getSpanishLeagueIds() {
        try {
            String endpoint = BASE_URL + "/leagues";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("x-rapidapi-key", API_KEY)
                    .header("x-rapidapi-host", "api-football-v1.p.rapidapi.com")
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray leagues = jsonResponse.getAsJsonObject("api").getAsJsonArray("leagues");

                System.out.println("IDs de las ligas en España:");
                for (int i = 0; i < leagues.size(); i++) {
                    JsonObject league = leagues.get(i).getAsJsonObject();
                    String country = league.get("country").getAsString();
                    String season = league.get("season").getAsString();
                    String name = league.get("name").getAsString();

                    if (country.equalsIgnoreCase("Spain") && season.equalsIgnoreCase(YEAR)) { {

                    }
                        if (name.equalsIgnoreCase("La Liga") || name.equalsIgnoreCase("Copa del Rey")) {
                            int leagueId = league.get("league_id").getAsInt();
                            String leagueName = league.get("name").getAsString();
                            System.out.printf("ID: %d, Nombre: %s%n", leagueId, leagueName);
                        }
                    }

                }
            } else {
                System.out.println("Error al obtener los datos: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Buscar partidos de ayer/hoy/mañana para cada liga

    public static void main(String[] args) {
        FootballService footballService = new FootballService();
        footballService.getSpanishLeagueIds();
    }
}
