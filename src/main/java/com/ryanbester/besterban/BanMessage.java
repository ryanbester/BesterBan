package com.ryanbester.besterban;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Helper class for reading and parsing ban messages.
 */
public class BanMessage {

    protected static String banMessage = "Banned";
    protected static String banChatMessage = "Banned";
    protected static String bedrockBanMessage = "Banned";

    /**
     * Loads a standard Java ban message from the file.
     *
     * @param plugin The plugin.
     */
    static void loadMessage(Plugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File banMessageFile = new File(plugin.getDataFolder(), "banmessage.txt");
        if (!banMessageFile.exists()) {
            try {
                banMessageFile.createNewFile();
                InputStream is = plugin.getResourceAsStream("banmessage.txt");
                OutputStream os = new FileOutputStream(banMessageFile);
                ByteStreams.copy(is, os);
            } catch (Exception e) {
                plugin.getLogger().warning("Cannot create ban message file");
            }
        }

        try {
            FileInputStream fis = new FileInputStream(banMessageFile);
            byte[] data = new byte[(int) banMessageFile.length()];
            fis.read(data);
            fis.close();

            banMessage = new String(data, StandardCharsets.UTF_8);
            if (banMessage.endsWith("\n")) {
                banMessage = banMessage.substring(0, banMessage.length() - 1);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Cannot load ban message file");
        }
    }

    /**
     * Loads a ban message from the file, for being displayed in chat.
     *
     * @param plugin The plugin.
     */
    static void loadChatMessage(Plugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File banMessageFile = new File(plugin.getDataFolder(), "banmessagechat.txt");
        if (!banMessageFile.exists()) {
            try {
                banMessageFile.createNewFile();
                InputStream is = plugin.getResourceAsStream("banmessagechat.txt");
                OutputStream os = new FileOutputStream(banMessageFile);
                ByteStreams.copy(is, os);
            } catch (Exception e) {
                plugin.getLogger().warning("Cannot create ban message file");
            }
        }

        try {
            FileInputStream fis = new FileInputStream(banMessageFile);
            byte[] data = new byte[(int) banMessageFile.length()];
            fis.read(data);
            fis.close();

            banChatMessage = new String(data, StandardCharsets.UTF_8);
            if (banChatMessage.endsWith("\n")) {
                banChatMessage = banChatMessage.substring(0, banChatMessage.length() - 1);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Cannot load ban message file");
        }
    }

    /**
     * Loads a Bedrock ban message from the file.
     *
     * @param plugin The plugin.
     */
    static void loadBedrockMessage(Plugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File banMessageFile = new File(plugin.getDataFolder(), "bedrockbanmessage.txt");
        if (!banMessageFile.exists()) {
            try {
                banMessageFile.createNewFile();
                InputStream is = plugin.getResourceAsStream("bedrockbanmessage.txt");
                OutputStream os = new FileOutputStream(banMessageFile);
                ByteStreams.copy(is, os);
            } catch (Exception e) {
                plugin.getLogger().warning("Cannot create ban message file");
            }
        }

        try {
            FileInputStream fis = new FileInputStream(banMessageFile);
            byte[] data = new byte[(int) banMessageFile.length()];
            fis.read(data);
            fis.close();

            bedrockBanMessage = new String(data, StandardCharsets.UTF_8);
            if (bedrockBanMessage.endsWith("\n")) {
                bedrockBanMessage = bedrockBanMessage.substring(0, bedrockBanMessage.length() - 1);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Cannot load ban message file");
        }
    }

    /**
     * Gets the ban message.
     *
     * @param banMessageInfo The ban message information.
     * @param bedrock        Whether the player is a Bedrock player or not.
     * @return The ban message.
     */
    public static BaseComponent getMessage(BanMessageInfo banMessageInfo, boolean bedrock) {
        String newMsg = banMessage;
        if (bedrock) {
            newMsg = bedrockBanMessage;
        }

        // Colours
        newMsg = newMsg.replaceAll("&", "§");

        // Type
        newMsg = newMsg.replaceAll(Pattern.quote("$type"),
            banMessageInfo.type == null ? "" : banMessageInfo.type);

        // Duration
        newMsg = newMsg.replaceAll(Pattern.quote("$duration"),
            banMessageInfo.duration == null ? "" : banMessageInfo.duration);

        // Reason
        newMsg = newMsg.replaceAll(Pattern.quote("$reason"),
            banMessageInfo.reason == null ? "" : banMessageInfo.reason);

        // Appeal ID
        newMsg = newMsg.replaceAll(Pattern.quote("$appealID"),
            banMessageInfo.appealID == null ? "" : banMessageInfo.appealID);

        return new TextComponent(newMsg);
    }

    /**
     * Gets the chat message.
     *
     * @param banMessageInfo The ban message information.
     * @return The ban message.
     */
    public static BaseComponent getChatMessage(BanMessageInfo banMessageInfo) {
        String newMsg = banChatMessage;

        // Colours
        newMsg = newMsg.replaceAll("&", "§");

        // Type
        newMsg = newMsg.replaceAll(Pattern.quote("$type"),
            banMessageInfo.type == null ? "" : banMessageInfo.type);

        // Duration
        newMsg = newMsg.replaceAll(Pattern.quote("$duration"),
            banMessageInfo.duration == null ? "" : banMessageInfo.duration);

        // Reason
        newMsg = newMsg.replaceAll(Pattern.quote("$reason"),
            banMessageInfo.reason == null ? "" : banMessageInfo.reason);

        // Appeal ID
        newMsg = newMsg.replaceAll(Pattern.quote("$appealID"),
            banMessageInfo.appealID == null ? "" : banMessageInfo.appealID);

        return new TextComponent(newMsg);
    }

}
