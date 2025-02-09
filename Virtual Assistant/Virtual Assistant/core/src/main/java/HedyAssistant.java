import java.util.List;

public class HedyAssistant {
    private final ResponseProcessor responseProcessor;
    private final InteractionHistory interactionHistory;

    public HedyAssistant(String configFilePath, String historyFilePath) {
        // Inicializar ConfigManager con el archivo de configuración
        ConfigManager configManager = new ConfigManager(configFilePath);
        configManager.loadConfig();

        // Obtener la clave de API para ChatGPT desde ConfigManager
        String apiKey = configManager.getConfig("apiKey");

        // Inicializar KeywordMatcher, ChatGPTClient y ResponseProcessor
        KeywordMatcher keywordMatcher = new KeywordMatcher();
        ChatGPTClient chatGPTClient = new ChatGPTClient(apiKey);
        this.responseProcessor = new ResponseProcessor(keywordMatcher, chatGPTClient);

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

    // Metodo para limpiar el historial
    public void clearHistory() {
        interactionHistory.clearHistory(); // Agrega un metodo en InteractionHistory para borrar datos
    }


}
