public class WakeWordTest {

    public static void main(String[] args) {
        String configFilePath = "config.json";
        ConfigManager configManager = new ConfigManager(configFilePath);
        configManager.loadConfig();
        try {
            String accessKey = ConfigManager.getConfig("picovoice_api_key");
            String modelPath = ConfigManager.getConfig("picovoice_model_path");
            String[] keywordPaths = {ConfigManager.getConfig("picovoice_keyword_path")};
            float[] sensitivities = {0.5f}; // Sensibilidad ajustable
            int audioDeviceIndex = -1; // -1 para dispositivo por defecto

            WakeWordDetector detector = new WakeWordDetector(accessKey, modelPath, keywordPaths, sensitivities, audioDeviceIndex);
            detector.startListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
