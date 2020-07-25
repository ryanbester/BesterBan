package com.ryanbester.besterban.messages;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * Helper class for displaying messages from the messages file.
 */
public class Messages {

    protected static Configuration messagesConfig;
    protected static HashMap<String, String> messages = new HashMap<>();

    /**
     * Loads the messages from the messages file.
     *
     * @param plugin The plugin.
     */
    public static void loadMessagesFile(Plugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try {
                messagesFile.createNewFile();
                InputStream is = plugin.getResourceAsStream("messages.yml");
                OutputStream os = new FileOutputStream(messagesFile);
                ByteStreams.copy(is, os);
            } catch (Exception e) {
                plugin.getLogger().warning("Cannot create messages file");
            }
        }

        try {
            messagesConfig = ConfigurationProvider.getProvider(YamlConfiguration.class)
                .load(messagesFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Cannot parse config file");
        }

        // Get messages
        for (String key : messagesConfig.getKeys()) {
            messages.put(key, messagesConfig.getString(key, ""));
        }
    }

    /**
     * Gets a message from the messatges file, parsing the colour codes and replacing the parameter
     * placeholders.
     *
     * @param name   The message name.
     * @param params The parameters to replace.
     * @return The complete message.
     */
    public static BaseComponent getMessage(String name, ParamsBuilder params) {
        String message = messages.get(name);
        if (message == null) {
            return new TextComponent("");
        }

        // Params
        for (Entry<String, String> param : params.params.entrySet()) {
            message = message.replaceAll(Pattern.quote("$" + param.getKey()), param.getValue());
        }

        // Colours
        message = message.replaceAll("&", "§");

        return new TextComponent(message);
    }

}
