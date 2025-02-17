package bbw.narratortest.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ModConfig {
    private static final Path CONFIG_PATH = Path.of("config/narrator_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class ConfigData {
        public double temperature = 0.3;
        public String systemPrompt = "You are a backseat gaming Minecraft player. You are very opinionated, mean, and nerdy";
        public int debugLevel = 1;
        public int narratorCooldown = 1000000;
        public int maxEvents = 40;
        public boolean useElevenLabs = false;
    }

    private static ConfigData config = new ConfigData();

    public static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                config = GSON.fromJson(reader, ConfigData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            saveConfig(); // Create default config if missing
        }
    }

    public static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(config), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigData getConfig() {
        return config;
    }
}