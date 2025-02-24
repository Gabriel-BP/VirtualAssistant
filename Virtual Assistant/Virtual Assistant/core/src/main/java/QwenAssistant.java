import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QwenAssistant {

    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "qwen2.5:7b";

    /**
     * Envía un prompt al modelo DeepSeek-R1 y devuelve la respuesta generada.
     *
     * @param prompt El texto del prompt que se enviará al modelo.
     * @return La respuesta completa generada por el modelo.
     * @throws Exception Si ocurre algún error durante la comunicación con la API.
     */
    public String generateResponse(String prompt) throws Exception {
        URL url = new URL(OLLAMA_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Construir el cuerpo de la solicitud JSON
        String jsonInputString = String.format("{\"model\": \"%s\", \"prompt\": \"%s\"}", MODEL_NAME, prompt);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Leer y procesar la respuesta incremental
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            JSONParser parser = new JSONParser();
            while ((line = br.readLine()) != null) {
                try {
                    JSONObject jsonObject = (JSONObject) parser.parse(line);
                    if (jsonObject.containsKey("response")) {
                        String response = (String) jsonObject.get("response");
                        responseBuilder.append(response.replace("\\n", "\n"));
                    }
                    if (jsonObject.containsKey("done") && (Boolean) jsonObject.get("done")) {
                        break; // Detener cuando la respuesta esté completa
                    }
                } catch (ParseException e) {
                    System.err.println("Error al analizar la respuesta JSON: " + e.getMessage());
                }
            }
        }

        connection.disconnect();

        return responseBuilder.toString().trim();
    }
}