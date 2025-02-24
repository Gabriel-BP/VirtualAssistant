public class ResponseProcessor {
    private final KeywordMatcher keywordMatcher;
    private final QwenAssistant qwenAssistant; // Cambio aquí: Usar QwenAssistant

    public ResponseProcessor(KeywordMatcher keywordMatcher, QwenAssistant qwenAssistant) {
        this.keywordMatcher = keywordMatcher;
        this.qwenAssistant = qwenAssistant;
    }

    public String processResponse(String input) {
        // Verificar si hay una coincidencia con las palabras clave
        String keywordResponse = keywordMatcher.matchKeyword(input);
        if (keywordResponse != null && !keywordResponse.isEmpty()) {
            return keywordResponse; // Si hay coincidencia, usar KeywordMatcher
        }

        try {
            // Si no hay coincidencia, usar QwenAssistant
            return qwenAssistant.generateResponse(input);
        } catch (Exception e) {
            e.printStackTrace();
            return "Lo siento, ocurrió un error al procesar tu solicitud.";
        }
    }
}