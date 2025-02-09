import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private final String configFilePath;
    private JsonObject config;

    public ConfigManager(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public void loadConfig() {
        try (FileReader reader = new FileReader(configFilePath)) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            System.out.println("Error loading config: " + e.getMessage());
            config = new JsonObject(); // Initialize as empty if file is missing or invalid
        }
    }

    public String getConfig(String key) {
        return config.has(key) ? config.get(key).getAsString() : "Unknown";
    }

    public void updateConfig(String key, String value) {
        config.addProperty(key, value);
        saveConfig();
    }

    private void saveConfig() {
        try (FileWriter writer = new FileWriter(configFilePath)) {
            new Gson().toJson(config, writer);
        } catch (IOException e) {
            System.out.println("Error saving config: " + e.getMessage());
        }
    }
}
