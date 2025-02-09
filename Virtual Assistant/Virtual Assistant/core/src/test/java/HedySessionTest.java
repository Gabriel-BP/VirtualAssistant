public class HedySessionTest {
    public static void main(String[] args) {
        String configFilePath = "config.json";
        String historyFilePath = "history.log"; // Archivo para guardar el historial

        HedyAssistant hedyAssistant = new HedyAssistant(configFilePath, historyFilePath);
        HedySession session = new HedySession(hedyAssistant, historyFilePath);

        session.start();
        session.printInteractionLog(); // Imprimir el historial al terminar
    }
}


