package com.ryanbester.besterban.command;

import com.ryanbester.besterban.BanMessage;
import com.ryanbester.besterban.BanMessageInfo;
import com.ryanbester.besterban.BesterBan;
import com.ryanbester.besterban.PlayerUtil;
import com.ryanbester.besterban.database.DatabaseHelper;
import com.ryanbester.besterban.messages.Messages;
import com.ryanbester.besterban.messages.ParamsBuilder;
import java.sql.SQLException;
import java.util.Arrays;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class NetworkBan extends Command {

    protected Plugin plugin;

    public NetworkBan(Plugin plugin) {
        super("networkban", "besterban.networkban");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        int numParams = strings.length;
        if (numParams < 1) {
            commandSender.sendMessage(Messages.getMessage("ban-specify-player", new ParamsBuilder()
                .add("type", "Network")));
            return;
        }
        if (numParams < 2) {
            commandSender.sendMessage(Messages.getMessage("ban-specify-reason", new ParamsBuilder()
                .add("type", "Network")
                .add("player", strings[0])));
            return;
        }

        String username = strings[0];
        final String reason = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));

        final boolean xuidGiven;

        // Check if username specified is XUID
        if (username.toLowerCase().startsWith("x:")) {
            username = username.substring(2);
            xuidGiven = true;
        } else {
            xuidGiven = false;
        }

        final String usernameFinal = username;

        // If player is Bedrock, check if they are online. Bedrock players cannot be banned when offline.
        ProxiedPlayer bedrockPlayer = null;
        if (PlayerUtil.isBedrock(username)) {
            bedrockPlayer = plugin.getProxy().getPlayer(username);
            if (bedrockPlayer == null) {
                commandSender.sendMessage(
                    Messages.getMessage("ban-bedrock-player-offline", new ParamsBuilder()
                        .add("type", "Network")
                        .add("player", username)));
                return;
            }
        }

        final String appealID;
        try {
            appealID = DatabaseHelper.generateAppealID();

            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                if (player.getName().equals(username)) {
                    // Kick player if they are online
                    ServerInfo serverInfo = plugin.getProxy()
                        .getServerInfo(BesterBan.fallbackServer);
                    // If player isn't on the fallback server, move them there first
                    if (!player.getServer().getInfo().equals(serverInfo)) {
                        player.connect(serverInfo, (aBoolean, throwable) ->
                            player.disconnect(BanMessage.getMessage(new BanMessageInfo(
                                "Network", "Permanent", reason, appealID
                            ), PlayerUtil.isBedrock(usernameFinal)))
                        );
                    } else {
                        player.disconnect(BanMessage.getMessage(new BanMessageInfo(
                            "Network", "Permanent", reason, appealID
                        ), PlayerUtil.isBedrock(strings[0])));
                    }
                }
            }
        } catch (SQLException e) {
            commandSender
                .sendMessage(Messages.getMessage("ban-error-generate-appeal-id", new ParamsBuilder()
                    .add("type", "Network")
                    .add("player", username)
                    .add("message", e.getLocalizedMessage())
                    .add("stacktrace", Arrays.toString(e.getStackTrace()))));
            return;
        }

        final ProxiedPlayer bedrockPlayerFinal = bedrockPlayer;

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
                        .sendMessage(Messages.getMessage("ban-error-uuid", new ParamsBuilder()
                            .add("type", "Network")
                            .add("player", usernameFinal)));
                    return;
                }

                String bannedBy = "Console";
                if (commandSender instanceof ProxiedPlayer) {
                    bannedBy = ((ProxiedPlayer) commandSender).getUniqueId().toString()
                        .replaceAll("-", "");
                }

                if (DatabaseHelper
                    .addBan(uuid, null, "Network", null, reason, appealID, bannedBy)
                    == null) {
                    commandSender
                        .sendMessage(Messages.getMessage("ban-error-adding", new ParamsBuilder()
                            .add("type", "Network")
                            .add("player", usernameFinal)
                            .add("uuid", uuid)));
                    return;
                }

                commandSender
                    .sendMessage(Messages.getMessage("ban-permanently-banned", new ParamsBuilder()
                        .add("type", "Network")
                        .add("player", usernameFinal)
                        .add("uuid", uuid)
                        .add("reason", reason)));
            } catch (SQLException e) {
                commandSender.sendMessage(Messages.getMessage("ban-error-sql", new ParamsBuilder()
                    .add("type", "Network")
                    .add("player", usernameFinal)
                    .add("message", e.getLocalizedMessage())
                    .add("stacktrace", Arrays.toString(e.getStackTrace()))));
            }
        }).start();
    }
}
