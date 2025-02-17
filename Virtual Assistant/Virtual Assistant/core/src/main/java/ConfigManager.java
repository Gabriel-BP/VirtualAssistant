import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private final String configFilePath;
    private static JsonObject config;

    public ConfigManager(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public void loadConfig() {
        try (FileReader reader = new FileReader(configFilePath)) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
            System.out.println("Loaded Config: " + config.toString());
        } catch (IOException e) {
            System.out.println("Error loading config: " + e.getMessage());
            config = new JsonObject(); // Initialize as empty if file is missing or invalid
        }
    }

    public static String getConfig(String key) {
        if (!config.has(key)) {
            return "Unknown";
        }
        return config.get(key).getAsString();
    }

    public static String[] getConfigArray(String key) {
        if (!config.has(key)) {
            return new String[0]; // Return an empty array if the key doesn't exist
        }
        JsonArray jsonArray = config.getAsJsonArray(key);
        String[] result = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            result[i] = jsonArray.get(i).getAsString();
        }
        return result;
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