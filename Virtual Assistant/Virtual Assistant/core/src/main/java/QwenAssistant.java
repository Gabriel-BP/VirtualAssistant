import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class QwenAssistant {

    private static final String OLLAMA_API_URL = "https://api.together.xyz/v1/chat/completions";
    private static final String MODEL_NAME = "meta-llama/Llama-4-Maverick-17B-128E-Instruct-FP8";
    private static final String SYSTEM_PROMPT = "Eres un asistente virtual que responde con información actualizada y precisa, " +
            "siendo lo más directo posible y evitando respuestas muy largas pero siendo extenso cuando se requiere. " +
            "No tienes acceso al historial de la conversación, por lo que evitarás respuestas que inciten a una respuesta de usuario";

    // La API_KEY se carga desde el archivo de configuración usando ConfigManager.
    private static final String API_KEY;

    static {
        // Se carga la configuración desde el archivo config.json
        ConfigManager configManager = new ConfigManager("Virtual Assistant/utils/config.json");
        configManager.loadConfig();
        API_KEY = ConfigManager.getConfig("together_api_key");
        if(API_KEY.equals("Unknown")){
            System.err.println("API key no encontrada en el archivo de configuración.");
        }
    }

    /**
     * Genera la respuesta procesando el JSON devuelto por el API.
     *
     * @param prompt  Mensaje del usuario.
     * @param history Historial de conversación (no se utiliza en este ejemplo).
     * @return El contenido del mensaje del asistente.
     * @throws Exception En caso de error de conexión o de procesamiento de JSON.
     */
    public String generateResponse(String prompt, String history) throws Exception {
        URL url = new URL(OLLAMA_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        // Construir el cuerpo de la solicitud en formato JSON
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", MODEL_NAME);
        jsonBody.put("stream", false);

        // Construir el array de mensajes con el mensaje del sistema y del usuario
        JSONArray messages = new JSONArray();

        // Mensaje del sistema
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messages.add(systemMessage);

        // Mensaje del usuario
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        jsonBody.put("messages", messages);

        // Convertir el JSON a cadena para enviarlo
        String jsonInputString = jsonBody.toJSONString();

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Leer la respuesta completa del servidor
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
        }
        String rawResponse = responseBuilder.toString();
        // Procesar el JSON de la respuesta
        JSONParser parser = new JSONParser();
        JSONObject responseJson = (JSONObject) parser.parse(rawResponse);

        // Extraer el array "choices" y luego el objeto "message" del primer elemento
        JSONArray choices = (JSONArray) responseJson.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new Exception("La respuesta del API no contiene el arreglo 'choices'");
        }
        JSONObject firstChoice = (JSONObject) choices.get(0);
        JSONObject messageObj = (JSONObject) firstChoice.get("message");
        if (messageObj == null) {
            throw new Exception("La respuesta del API no contiene el objeto 'message' en 'choices'");
        }
        String content = (String) messageObj.get("content");

        connection.disconnect();
        return content;
    }

}
