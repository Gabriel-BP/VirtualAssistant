import okhttp3.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class ChatGPTClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;

    public ChatGPTClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getResponse(String prompt) {
        OkHttpClient client = new OkHttpClient();

        // Crear el objeto de mensaje
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        // Crear el JSON para el cuerpo de la solicitud
        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("model", "gpt-4o-mini"); // Ajuste el modelo al valor del curl
        JsonArray messagesArray = new JsonArray();
        messagesArray.add(message); // Añadir el mensaje al arreglo
        requestBodyJson.add("messages", messagesArray);
        requestBodyJson.addProperty("temperature", 0.7);

        // Crear el cuerpo de la solicitud
        RequestBody requestBody = RequestBody.create(
                requestBodyJson.toString(),
                MediaType.get("application/json")
        );

        // Configurar la solicitud
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();

        // Enviar la solicitud y procesar la respuesta
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject responseJson = JsonParser.parseString(response.body().string()).getAsJsonObject();

                // Obtener el contenido de la respuesta correctamente
                return responseJson
                        .getAsJsonArray("choices")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message") // Acceder a "message"
                        .get("content") // Obtener el contenido del mensaje
                        .getAsString();
            } else {
                System.out.println("Error: " + response.code() + " - " + response.message());
                return "Lo siento, no pude procesar tu solicitud.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Ocurrió un error al conectar con ChatGPT.";
        }
    }
}
