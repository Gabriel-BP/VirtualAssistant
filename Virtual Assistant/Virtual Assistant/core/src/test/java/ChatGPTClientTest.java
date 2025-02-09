public class ChatGPTClientTest {
    public static void main(String[] args) {
        // Asegúrate de que la clave de API esté correctamente configurada en el archivo config.json
        String apiKey = "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"; // Sustituye esto por tu clave de API real

        // Crear instancia del cliente ChatGPT
        ChatGPTClient chatGPTClient = new ChatGPTClient(apiKey);

        // Mensaje de prueba
        String testPrompt = "¿Cuál es la capital de Francia?";

        // Obtener respuesta de ChatGPT
        String response = chatGPTClient.getResponse(testPrompt);

        // Mostrar la respuesta en consola
        System.out.println("Respuesta de ChatGPT: " + response);
    }
}