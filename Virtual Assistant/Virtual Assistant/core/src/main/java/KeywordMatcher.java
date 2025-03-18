import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import OpenNLP.OpenNLPAnalyzer;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KeywordMatcher {
    public String matchKeyword(String input) throws Exception {
        input = input.toLowerCase(); // Convertir a minúsculas para facilitar la comparación

        String intention = OpenNLPAnalyzer.analize(input);

        if (intention == null) {
            return null;
        }

        if (intention.equals("decir_hora")) {
            return decir_hora();
        }

        if (intention.equals("decir_fecha")) {
            return decir_fecha();
        }

        if (intention.equals("consultar_clima")) {
            return consultarClimaDefault();
        }

        if (intention.equals("consultar_clima_lugar")) {
            return consultarClimaLugar("Paris"); // Cambiar por la real usando GOOGLE NLP
        }
        if (intention.equals("consultar_cartelera")) {
            return consultarCartelera(List.of(), false); // Solo nombres por ahora
        }

        if (intention.equals("consultar_estrenos")) {
            return consultarUpcomingCartelera(List.of(), false); // Solo nombres por ahora
        }

        // Si no coincide con ninguna palabra clave, retorna null
        return null;
    }

    // Función para obtener la hora actual en el formato "Son las 8 y 40"
    public static String decir_hora() {
        LocalDateTime ahora = LocalDateTime.now();
        int hora = ahora.getHour();
        int minutos = ahora.getMinute();
        return String.format("Son las %d y %d", hora, minutos);
    }

    // Función para obtener la fecha actual en el formato "Hoy es viernes 14 de marzo de 2025"
    public static String decir_fecha() {
        LocalDateTime ahora = LocalDateTime.now();
        String diaSemana = ahora.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("es", "ES"));
        int diaMes = ahora.getDayOfMonth();
        String mes = ahora.getMonth().getDisplayName(TextStyle.FULL, Locale.of("es", "ES"));
        int anio = ahora.getYear();
        return String.format("Hoy es %s %d de %s de %d", diaSemana, diaMes, mes, anio);
    }

    // Función para obtener el clima
    public static String consultarClimaDefault() throws Exception {
        OpenMeteoAPI apiDefault = new OpenMeteoAPI();
        return apiDefault.fetchWeatherData(0);
    }

    // Función para obtener el clima en un lugar
    public static String consultarClimaLugar(String lugar) throws Exception {
        LocationCoordinates coordinates = NominatimGeocodingService.getCoordinates(lugar);
        OpenMeteoAPI apiLugar = new OpenMeteoAPI(coordinates.getLatitude(), coordinates.getLongitude());
        return apiLugar.fetchWeatherData(1);
    }

    public static String consultarCartelera(List<String> campos, boolean incluirDetalles) throws IOException {
        StringBuilder resultado = new StringBuilder("Las películas en cartelera son:\n");
        String jsonStr = new String(Files.readAllBytes(Paths.get("Virtual Assistant/utils/current_movies_formatted.json")));
        JSONObject cartelera = new JSONObject(jsonStr);

        for (String pelicula : cartelera.keySet()) {
            resultado.append("- ").append(pelicula).append("\n");

            if (incluirDetalles) {
                JSONObject detalles = cartelera.getJSONObject(pelicula);
                for (String campo : campos) {
                    if (detalles.has(campo)) {
                        resultado.append("  ").append(campo).append(": ").append(detalles.get(campo)).append("\n");
                    } else {
                        resultado.append("  ").append(campo).append(": No disponible\n");
                    }
                }
                resultado.append("\n"); // Salto de línea entre películas
            }
        }

        return resultado.toString();
    }

    public static String consultarUpcomingCartelera(List<String> campos, boolean incluirDetalles) throws IOException {
        StringBuilder resultado = new StringBuilder("Los próximos estrenos son:\n");
        String jsonStr = new String(Files.readAllBytes(Paths.get("Virtual Assistant/utils/upcoming_movies_formatted.json")));
        JSONObject upcoming = new JSONObject(jsonStr);

        for (String pelicula : upcoming.keySet()) {
            resultado.append("- ").append(pelicula).append("\n");

            if (incluirDetalles) {
                JSONObject detalles = upcoming.getJSONObject(pelicula);
                for (String campo : campos) {
                    if (detalles.has(campo)) {
                        resultado.append("  ").append(campo).append(": ").append(detalles.get(campo)).append("\n");
                    } else {
                        resultado.append("  ").append(campo).append(": No disponible\n");
                    }
                }
                resultado.append("\n"); // Salto de línea entre películas
            }
        }

        return resultado.toString();
    }

    public static void main(String[] args) throws Exception {
        // Mostrar la hora actual
        System.out.println(decir_hora());

        // Mostrar la fecha actual
        System.out.println(decir_fecha());

        // Decir clima -predeterminado-
        OpenMeteoAPI apiDefault = new OpenMeteoAPI();
        System.out.println(apiDefault.fetchWeatherData(0));

        // Decir clima -lugar-
        String locationName = "Paris";
        LocationCoordinates coordinates = NominatimGeocodingService.getCoordinates(locationName);
        OpenMeteoAPI apiLugar = new OpenMeteoAPI(coordinates.getLatitude(), coordinates.getLongitude());
        System.out.println(apiLugar.fetchWeatherData(1));

        // Consultar cartelera (solo nombres)
        System.out.println(consultarCartelera(List.of(), false));

        // Consultar upcoming cartelera (solo nombres)
        System.out.println(consultarUpcomingCartelera(List.of(), false));

        // Ejemplo: Consultar cartelera con detalles (para futuras pruebas)
        // System.out.println(consultarCartelera(List.of("release_date", "overview", "vote_average"), true));
    }
}