public class HedyAssistantTest {
    public static void main(String[] args) {
        String configFilePath = "utils/config.json";
        String historyFilePath = "utils/history.log"; // Archivo para historial

        HedyAssistant hedyAssistant = new HedyAssistant(configFilePath, historyFilePath);

        String response = hedyAssistant.processInput("¿Qué hora es?");
        System.out.println("Hedy: " + response);
    }
}


