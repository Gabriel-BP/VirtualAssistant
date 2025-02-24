import java.util.Scanner;

public class DeepseekAssistantTest {

    public static void main(String[] args) {
        DeepseekAssistant assistant = new DeepseekAssistant();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Bienvenido al asistente virtual con DeepSeek-R1!");
        System.out.println("Escribe 'salir' para terminar la sesión.");

        while (true) {
            System.out.print("\nTú: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("salir")) {
                System.out.println("Sesión terminada. ¡Hasta luego!");
                break;
            }

            try {
                String modelResponse = assistant.generateResponse(userInput);
                System.out.println("DeepSeek-R1: " + modelResponse);
            } catch (Exception e) {
                System.err.println("Error al generar respuesta: " + e.getMessage());
            }
        }

        scanner.close();
    }
}