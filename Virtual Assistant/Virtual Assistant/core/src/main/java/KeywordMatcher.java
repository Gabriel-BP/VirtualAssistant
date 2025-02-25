import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class KeywordMatcher {
    public String matchKeyword(String input) {
        input = input.toLowerCase(); // Convertir a minúsculas para facilitar la comparación

        // Detectar frases relacionadas con la hora
        if (input.contains("qué hora es") || input.contains("hora")) {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return "Son las " + currentTime.format(formatter);
        }

        // Detectar frases relacionadas con la fecha
        if (input.contains("qué día es") || input.contains("día") || input.contains("dia")) {
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return "Hoy es " + currentDate.format(formatter);
        }

        // Si no coincide con ninguna palabra clave, retorna null
        return null;
    }
}

