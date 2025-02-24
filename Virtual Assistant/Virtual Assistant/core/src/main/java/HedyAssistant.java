import java.util.List;

public class HedyAssistant {
    private final ResponseProcessor responseProcessor;
    private final InteractionHistory interactionHistory;

    public HedyAssistant(String configFilePath, String historyFilePath) {
        // Inicializar ConfigManager con el archivo de configuración
        ConfigManager configManager = new ConfigManager(configFilePath);
        configManager.loadConfig();

        // Inicializar KeywordMatcher y QwenAssistant
        KeywordMatcher keywordMatcher = new KeywordMatcher();
        QwenAssistant qwenAssistant = new QwenAssistant(); // Cambio aquí: Usar QwenAssistant
        this.responseProcessor = new ResponseProcessor(keywordMatcher, qwenAssistant);

        // Inicializar InteractionHistory con la ruta del archivo
        this.interactionHistory = new InteractionHistory(historyFilePath);
    }

    public String processInput(String input) {
        // Procesar la respuesta del asistente
        String response = responseProcessor.processResponse(input);

        // Agregar interacción al historial
        interactionHistory.addInteraction(input, response);

        return response;
    }

    public List<String> getInteractionHistory() {
        return interactionHistory.getAllInteractions();
    }

    public void clearHistory() {
        interactionHistory.clearHistory(); // Limpia el historial
    }
}