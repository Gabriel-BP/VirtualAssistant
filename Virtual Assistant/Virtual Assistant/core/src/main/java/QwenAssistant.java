import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("ALL")
public class QwenAssistant {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/chat";  // Cambiado a endpoint chat
    private static final String MODEL_NAME = "qwen2.5:14b";
    private static final String SYSTEM_PROMPT =
            "Eres un asistente virtual llamado Hedy que fue desarrollada por el Museo Elder de la Ciencia y la Tecnología. Intentas mantener las respuestas lo más breve que puedas, pero sin perder tu naturalidad. ";

    /**
     * Envía un prompt al modelo Qwen 2.5 y devuelve la respuesta completa de una sola vez.
     *
     * @param prompt El texto del prompt que se enviará al modelo.
     * @param history El historial de la conversación (si es necesario).
     * @return La respuesta completa generada por el modelo.
     * @throws Exception Si ocurre algún error durante la comunicación con la API.
     */
    public String generateResponse(String prompt, String history) throws Exception {
        URL url = new URL(OLLAMA_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Construir el cuerpo de la solicitud JSON utilizando el formato de chat
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", MODEL_NAME);
        jsonBody.put("stream", false);  // Desactivar streaming

        // Crear el mensaje del usuario
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        // Crear array de mensajes (se podría expandir para incluir el historial)
        org.json.simple.JSONArray messages = new org.json.simple.JSONArray();

        // Añadir el mensaje del sistema primero
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messages.add(systemMessage);

        // Añadir el mensaje del usuario
        messages.add(message);

        jsonBody.put("messages", messages);

        String jsonInputString = jsonBody.toJSONString();

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Leer la respuesta completa
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
        }

        // Procesar la respuesta JSON para extraer el contenido
        JSONParser parser = new JSONParser();
        JSONObject responseJson = (JSONObject) parser.parse(responseBuilder.toString());

        // En el API de chat, la respuesta está en el campo "message" -> "content"
        JSONObject messageObj = (JSONObject) responseJson.get("message");
        String content = (String) messageObj.get("content");

        connection.disconnect();
        return content;
    }
}