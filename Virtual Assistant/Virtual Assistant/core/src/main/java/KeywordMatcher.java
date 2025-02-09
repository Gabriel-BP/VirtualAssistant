import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class KeywordMatcher {
    public String matchKeyword(String input) {
        input = input.toLowerCase(); // Convertir a minúsculas para facilitar la comparación

        // Detectar frases relacionadas con la hora
        if (input.contains("¿qué hora es") || input.contains("hora") || input.contains("son las")) {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return "Son las " + currentTime.format(formatter);
        }

        // Si no coincide con ninguna palabra clave, retorna null
        return null;
    }
}

