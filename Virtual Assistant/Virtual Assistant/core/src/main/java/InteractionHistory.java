import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InteractionHistory {
    private final List<String> history;
    private final String historyFilePath;

    public InteractionHistory(String historyFilePath) {
        this.historyFilePath = historyFilePath;
        this.history = new ArrayList<>();
        loadHistory(); // Cargar historial existente al inicializar
    }

    public void addInteraction(String userInput, String assistantResponse) {
        String timestamp = getCurrentTimestamp();
        String interaction = timestamp + " Usuario: " + userInput + "\n" + timestamp + " Hedy: " + assistantResponse;
        history.add(interaction);
        saveHistory(); // Guardar automáticamente después de añadir una interacción
    }

    public void addEvent(String eventDescription) {
        String timestamp = getCurrentTimestamp();
        String event = timestamp + " [Evento] " + eventDescription;
        history.add(event);
        saveHistory(); // Guardar automáticamente después de añadir un evento
    }

    public List<String> getAllInteractions() {
        return new ArrayList<>(history);
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void saveHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFilePath))) {
            for (String entry : history) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar el historial: " + e.getMessage());
        }
    }

    private void loadHistory() {
        File historyFile = new File(historyFilePath);
        if (!historyFile.exists()) return; // Si no existe, no cargar nada

        try (BufferedReader reader = new BufferedReader(new FileReader(historyFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error al cargar el historial: " + e.getMessage());
        }
    }

    public void clearHistory() {
        history.clear(); // Limpia la lista de historial en memoria
        try (FileWriter writer = new FileWriter("history.log", false)) {
            writer.write(""); // Borra el contenido del archivo
        } catch (IOException e) {
            System.out.println("Error al limpiar el archivo de historial: " + e.getMessage());
        }
    }

}
