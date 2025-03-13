import java.io.IOException;
import java.util.List;

public class HedyAssistant {
    private final ResponseProcessor responseProcessor;
    private final InteractionHistory interactionHistory;

    public HedyAssistant(String configFilePath, String historyFilePath) throws IOException {
        ConfigManager configManager = new ConfigManager(configFilePath);
        configManager.loadConfig();

        KeywordMatcher keywordMatcher = new KeywordMatcher();
        QwenAssistant qwenAssistant = new QwenAssistant();
        this.responseProcessor = new ResponseProcessor(keywordMatcher, qwenAssistant);

        this.interactionHistory = new InteractionHistory(historyFilePath);
    }

    public String processInput(String input) throws IOException {
        // Get the current session's history
        List<String> history = getInteractionHistory();

        // Convert the history to a string format (e.g., JSON or plain text)
        StringBuilder historyString = new StringBuilder();
        for (String entry : history) {
            historyString.append(entry).append("\n");
        }

        // Process the response using the input and history
        String response = responseProcessor.processResponse(input, historyString.toString());

        // Add the interaction to the current session's history
        interactionHistory.addInteraction(input, response);

        return response;
    }

    public List<String> getInteractionHistory() {
        return interactionHistory.getAllInteractions();
    }

    public void clearHistory() {
        interactionHistory.clearHistory(); // Limpia el historial
    }

    private String getHistoryContent() {
        StringBuilder historyBuilder = new StringBuilder();
        for (String entry : interactionHistory.getAllInteractions()) {
            historyBuilder.append(entry).append("\n");
        }
        return historyBuilder.toString();
    }
}