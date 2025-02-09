public class ResponseProcessor {
    private final KeywordMatcher keywordMatcher;
    private final ChatGPTClient chatGPTClient;

    public ResponseProcessor(KeywordMatcher keywordMatcher, ChatGPTClient chatGPTClient) {
        this.keywordMatcher = keywordMatcher;
        this.chatGPTClient = chatGPTClient;
    }

    public String processResponse(String input) {
        // Intentar primero con respuestas predefinidas
        String predefinedResponse = keywordMatcher.matchKeyword(input);

        if (predefinedResponse != null) {
            return predefinedResponse; // Retornar respuesta predefinida si existe
        }

        // Si no coincide con palabras clave, usar ChatGPT
        return chatGPTClient.getResponse(input);
    }
}
