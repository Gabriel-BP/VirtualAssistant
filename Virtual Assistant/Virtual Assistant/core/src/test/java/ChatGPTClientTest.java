public class ChatGPTClientTest {
    public static void main(String[] args) {
        String configFilePath = "config.json";

        ConfigManager configManager = new ConfigManager(configFilePath);
        configManager.loadConfig();
        // Asegúrate de que la clave de API esté correctamente configurada en el archivo config.json
        String apiKey = ConfigManager.getConfig("openai_api_key");

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