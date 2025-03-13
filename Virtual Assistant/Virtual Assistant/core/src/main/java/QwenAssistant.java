// QwenAssistant.java - Módulo para interactuar con el modelo Qwen a través de Ollama
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("ALL")
public class QwenAssistant {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "qwen2.5:7b";
    private static final String SYSTEM_PROMPT =
            "Eres un asistente virtual llamado Hedy que fue desarrollada por el Museo Elder de la Ciencia y la Tecnología. Intentas mantener las respuestas lo más breve que puedas, pero sin perder tu naturalidad. ";

    /**
     * Envía un prompt al modelo Qwen 2.5 y devuelve la respuesta generada.
     *
     * @param prompt El texto del prompt que se enviará al modelo.
     * @param history Historial opcional de conversación.
     * @return La respuesta completa generada por el modelo.
     * @throws Exception Si ocurre algún error durante la comunicación con la API.
     */
    public String generateResponse(String prompt, String history) throws Exception {
        StringBuilder responseBuilder = new StringBuilder();

        // Llamar al metodo de streaming y recopilar la respuesta completa
        generateResponseStream(prompt, history, chunk -> {
            responseBuilder.append(chunk);
        });

        return responseBuilder.toString().trim();
    }

    /**
     * Envía un prompt al modelo Qwen 2.5 y procesa la respuesta como un stream
     * a través del callback proporcionado.
     *
     * @param prompt El texto del prompt que se enviará al modelo.
     * @param history Historial opcional de conversación.
     * @param streamConsumer Un Consumer que procesa cada fragmento de texto generado.
     * @throws Exception Si ocurre algún error durante la comunicación con la API.
     */
    public void generateResponseStream(String prompt, String history, Consumer<String> streamConsumer) throws Exception {
        URL url = new URL(OLLAMA_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Construir el cuerpo de la solicitud JSON con el campo "system"
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", MODEL_NAME);
        jsonBody.put("prompt", prompt);
        jsonBody.put("system", SYSTEM_PROMPT);
        jsonBody.put("stream", true); // Asegurar que Ollama está configurado para streaming

        String jsonInputString = jsonBody.toJSONString();

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Leer y procesar la respuesta incremental
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            JSONParser parser = new JSONParser();
            while ((line = br.readLine()) != null) {
                try {
                    JSONObject jsonObject = (JSONObject) parser.parse(line);
                    if (jsonObject.containsKey("response")) {
                        String response = (String) jsonObject.get("response");
                        String formattedResponse = response.replace("\\n", "\n");

                        // Enviar el fragmento al consumidor
                        streamConsumer.accept(formattedResponse);
                    }
                    if (jsonObject.containsKey("done") && (Boolean) jsonObject.get("done")) {
                        break; // Detener cuando la respuesta esté completa
                    }
                } catch (ParseException e) {
                    System.err.println("Error al analizar la respuesta JSON: " + e.getMessage());
                }
            }
        } finally {
            connection.disconnect();
        }
    }
}