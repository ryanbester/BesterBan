package com.ryanbester.besterban.command;

import com.ryanbester.besterban.PlayerUtil;
import com.ryanbester.besterban.database.DatabaseHelper;
import com.ryanbester.besterban.messages.Messages;
import com.ryanbester.besterban.messages.ParamsBuilder;
import java.sql.SQLException;
import java.util.Arrays;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class Unban extends Command {

    protected Plugin plugin;

    public Unban(Plugin plugin) {
        super("unban", "besterban.unban");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        int numParams = strings.length;
        if (numParams < 1) {
            commandSender
                .sendMessage(Messages.getMessage("unban-specify-player", new ParamsBuilder()));
            return;
        }

        String username = strings[0];

        final String server;
        if (numParams > 1) {
            server = strings[1];
        } else {
            server = null;
        }

        final boolean xuidGiven;

        // Check if username specified is XUID
        if (username.toLowerCase().startsWith("x:")) {
            username = username.substring(2);
            xuidGiven = true;
        } else {
            xuidGiven = false;
        }

        // If player is Bedrock, check if they are online. Bedrock players cannot be unbanned when offline.
        ProxiedPlayer bedrockPlayer = null;
        if (PlayerUtil.isBedrock(username)) {
            bedrockPlayer = plugin.getProxy().getPlayer(username);
            if (bedrockPlayer == null) {
                commandSender.sendMessage(
                    Messages.getMessage("unban-bedrock-player-offline", new ParamsBuilder()
                        .add("player", username)));
                return;
            }
        }

        final ProxiedPlayer bedrockPlayerFinal = bedrockPlayer;
        final String usernameFinal = username;

        new Thread(() -> {
            try {
                String uuid = PlayerUtil.getUUID(usernameFinal);
                if (xuidGiven) {
                    uuid = PlayerUtil.getUUIDFromXUID(usernameFinal);
                } else if (PlayerUtil.isBedrock(usernameFinal)) {
                    uuid = bedrockPlayerFinal.getUniqueId().toString().replaceAll("-", "");
                }
                if (uuid == null) {
                    commandSender
                        .sendMessage(Messages.getMessage("unban-error-uuid", new ParamsBuilder()
                            .add("player", usernameFinal)));
                    return;
                }

                if (server != null) {
                    if (!DatabaseHelper.removeBan(uuid, server)) {
                        commandSender.sendMessage(
                            Messages.getMessage("unban-error-removing", new ParamsBuilder()
                                .add("player", usernameFinal)
                                .add("uuid", uuid)));
                        return;
                    }
                } else {
                    if (!DatabaseHelper.removeBan(uuid)) {
                        commandSender.sendMessage(
                            Messages.getMessage("unban-error-removing", new ParamsBuilder()
                                .add("player", usernameFinal)
                                .add("uuid", uuid)));
                        return;
                    }
                }

                commandSender.sendMessage(Messages.getMessage("unban-unbanned", new ParamsBuilder()
                    .add("player", usernameFinal)
                    .add("uuid", uuid)));
            } catch (SQLException e) {
                commandSender.sendMessage(Messages.getMessage("unban-error-sql", new ParamsBuilder()
                    .add("player", usernameFinal)
                    .add("message", e.getLocalizedMessage())
                    .add("stacktrace", Arrays.toString(e.getStackTrace()))));
            }
        }).start();
    }
}
