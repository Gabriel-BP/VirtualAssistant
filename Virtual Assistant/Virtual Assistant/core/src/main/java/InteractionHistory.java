import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class InteractionHistory {
    private final List<String> history;
    private final String historyFilePath;

    public InteractionHistory(String historyFilePath) {
        this.historyFilePath = historyFilePath;
        this.history = new ArrayList<>();
    }

    public void addEvent(String event) {
        String formattedEvent = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + event;
        history.add(formattedEvent);
        saveHistory();
    }

    public void addInteraction(String input, String response) {
        history.add("User: " + input);
        history.add("Hedy: " + response);
        saveHistory();
    }

    public List<String> getAllInteractions() {
        return new ArrayList<>(history);
    }

    private void loadHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            // If the file doesn't exist or is empty, do nothing
        }
    }

    public void clearHistory() {
        history.clear(); // Limpia la lista interna

        try (FileWriter writer = new FileWriter(historyFilePath, false)) {
            // Sobrescribe el archivo para eliminar todas las entradas antiguas
        } catch (IOException e) {
            System.err.println("Error al limpiar el historial: " + e.getMessage());
        }
    }

    private void saveHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFilePath))) {
            for (String entry : history) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }
}