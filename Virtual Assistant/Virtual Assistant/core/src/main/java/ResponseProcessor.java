import java.io.IOException;

public class ResponseProcessor {
    private final KeywordMatcher keywordMatcher;
    private final QwenAssistant qwenAssistant;

    public ResponseProcessor(KeywordMatcher keywordMatcher, QwenAssistant qwenAssistant) {
        this.keywordMatcher = keywordMatcher;
        this.qwenAssistant = qwenAssistant;
    }

    public String processResponse(String input, String history) throws Exception {
        // Check for keyword matches first
        String keywordResponse = keywordMatcher.matchKeyword(input);
        if (keywordResponse != null && !keywordResponse.isEmpty()) {
            return keywordResponse; // Return a predefined response if a keyword is matched
        }

        try {
            // Si no hay coincidencia, usar QwenAssistant
            return qwenAssistant.generateResponse(input, history);
        } catch (Exception e) {
            e.printStackTrace();
            return "Lo siento, ocurrió un error al procesar tu solicitud.";
        }
    }
}