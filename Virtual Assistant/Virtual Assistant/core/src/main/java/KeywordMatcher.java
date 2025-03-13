import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class KeywordMatcher {
    public String matchKeyword(String input) throws IOException {
        input = input.toLowerCase(); // Convertir a minúsculas para facilitar la comparación


        if (input.contains("hora")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDate date = LocalDate.parse(input, formatter);
            LocalTime time = LocalTime.parse(input, formatter);
            return date.format(formatter) + " " + time.format(formatter);
        }

        // Si no coincide con ninguna palabra clave, retorna null
        return null;
    }
}

