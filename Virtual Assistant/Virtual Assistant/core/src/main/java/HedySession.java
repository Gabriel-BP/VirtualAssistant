import java.util.Scanner;

public class HedySession {
    private final HedyAssistant hedyAssistant;
    private final InteractionHistory interactionHistory;
    private boolean isActive;

    public HedySession(HedyAssistant hedyAssistant, String historyFilePath) {
        this.hedyAssistant = hedyAssistant;
        this.interactionHistory = new InteractionHistory(historyFilePath);
        this.isActive = true;
    }

    public void start() {
        interactionHistory.addEvent("Sesión iniciada.");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hedy está lista para responder tus preguntas. Escribe 'salir' para terminar la sesión.");

        while (isActive) {
            System.out.print("Tú: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("salir")) {
                System.out.println("Finalizando la sesión. ¡Hasta luego!");
                interactionHistory.addEvent("Sesión finalizada.");
                isActive = false;
            } else {
                String response = hedyAssistant.processInput(userInput);
                System.out.println("Hedy: " + response);
                interactionHistory.addInteraction(userInput, response); // Registrar interacción
            }
        }

        scanner.close();
    }

    public void printInteractionLog() {
        System.out.println("\nHistorial completo de interacciones y eventos:");
        for (String log : interactionHistory.getAllInteractions()) {
            System.out.println(log);
        }
    }
}


