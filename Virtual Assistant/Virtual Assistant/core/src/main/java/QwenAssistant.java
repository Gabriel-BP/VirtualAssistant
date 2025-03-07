import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("ALL")
public class QwenAssistant {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "qwen2.5:14b";
    private static final String SYSTEM_PROMPT =
            "Eres un asistente virtual llamado Hedy que fue desarrollada por el Museo Elder de la Ciencia y la Tecnología. Intentas mantener las respuestas lo más breve que puedas, pero sin perder tu naturalidad. Además, tienes acceso a un historial que solo usaras si se requiere y ha sido pedido expresamente por el usuario, es decir, si se pide información adicional sobre una pregunta anterior o sobre más información sobre una respuesta de Hedy, pero solo si lo pide el usuario.";

    /**
     * Envía un prompt al modelo Qwen 2.5 y devuelve la respuesta generada.
     *
     * @param prompt El texto del prompt que se enviará al modelo.
     * @return La respuesta completa generada por el modelo.
     * @throws Exception Si ocurre algún error durante la comunicación con la API.
     */
    public String generateResponse(String prompt, String history) throws Exception {
        URL url = new URL(OLLAMA_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        prompt = "Responde basandote en este input: " + prompt + " [Historial: " + history + "]";

        // Construir el cuerpo de la solicitud JSON con el campo "system"
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", MODEL_NAME);
        jsonBody.put("prompt", prompt); // Solo el prompt dinámico
        jsonBody.put("system", SYSTEM_PROMPT); // El sistema prompt va aquí

        String jsonInputString = jsonBody.toJSONString();

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