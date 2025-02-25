public class QwenAssistantTest {
    public static void main(String[] args) {
        QwenAssistant assistant = new QwenAssistant();

        try {
            String response = assistant.generateResponse("Hola!");
            System.out.println("Respuesta: " + response);
        } catch (Exception e) {
            System.err.println("Error al generar respuesta: " + e.getMessage());
        }
    }
}