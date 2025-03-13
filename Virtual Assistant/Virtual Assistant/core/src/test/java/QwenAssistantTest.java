public class QwenAssistantTest {
    public static void main(String[] args) {
        QwenAssistant assistant = new QwenAssistant();
        String history = "";

        long startTime = System.nanoTime(); // Iniciar el cronómetro

        try {
            String response = assistant.generateResponse("Dime un dato curioso", history);
            long endTime = System.nanoTime(); // Detener el cronómetro
            OutputModule.ttsSpeech(response);
            double elapsedTimeInSeconds = (endTime - startTime) / 1.0e9; // Convertir nanosegundos a segundos

            System.out.println("Respuesta: " + response);
            System.out.println("Tiempo total de ejecución: " + elapsedTimeInSeconds + " segundos");
        } catch (Exception e) {
            System.err.println("Error al generar respuesta: " + e.getMessage());
        }
    }
}
