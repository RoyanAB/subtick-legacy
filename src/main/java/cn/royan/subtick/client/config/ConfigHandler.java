package cn.royan.subtick.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import malilib.config.ModConfig;
import malilib.config.category.ConfigOptionCategory;
import malilib.config.util.JsonConfigUtils;
import malilib.util.data.ModInfo;
import malilib.util.data.json.JsonUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class ConfigHandler implements ModConfig {
    @Override
    public ModInfo getModInfo() {
        return Configs.MOD_INFO;
    }

    @Override
    public String getConfigFileName() {
        return "subtick.json";
    }

    @Override
    public List<ConfigOptionCategory> getConfigOptionCategories() {
        return Configs.CATEGORIES;
    }

    @Override
    public int getConfigVersion() {
        return 0;
    }

    @Override
    public void loadFromFile(Path configFile) {
        File settingFile = new File(configFile.toUri());
        if (settingFile.isFile() && settingFile.exists()) {
            JsonElement jsonElement = JsonUtils.parseJsonFile(configFile);
            if (jsonElement instanceof JsonObject) {
                JsonConfigUtils.loadFromFile(configFile, this.getConfigOptionCategories(), (a,b) ->{});
            }
        }
    }

    @Override
    public boolean saveToFile(Path configDirectory, Path configFile) {
        if ((configDirectory.toFile().exists() && configDirectory.toFile().isDirectory()) || configDirectory.toFile().mkdirs()) {
            JsonConfigUtils.saveToFile(configFile, this.getConfigOptionCategories(), 1);
        }
        return false;
    }
}
