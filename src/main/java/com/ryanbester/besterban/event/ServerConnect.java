package com.ryanbester.besterban.event;

import com.ryanbester.besterban.BanMessage;
import com.ryanbester.besterban.BanMessageInfo;
import com.ryanbester.besterban.BesterBan;
import com.ryanbester.besterban.database.DatabaseHelper;
import com.ryanbester.besterban.messages.Messages;
import com.ryanbester.besterban.messages.ParamsBuilder;
import java.sql.SQLException;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class ServerConnect implements Listener {

    private Plugin plugin;

    public ServerConnect(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        try {
            String uuid = event.getPlayer().getUniqueId().toString().replaceAll("-", "");
            String server = event.getTarget().getName();

            if (event.getReason() == Reason.JOIN_PROXY) {
                if (DatabaseHelper.checkServerBan(uuid, server)) {
                    BanMessageInfo banInfo = DatabaseHelper.getBanInfo(uuid);
                    if (banInfo == null) {
                        event.getPlayer().disconnect(
                            Messages.getMessage("error-checking-ban", new ParamsBuilder()));
                    } else {
                        event.getPlayer().sendMessage(BanMessage.getChatMessage(banInfo));
                        ServerInfo fallbackServer = plugin.getProxy()
                            .getServerInfo(BesterBan.fallbackServer);
                        event.getPlayer().connect(fallbackServer);
                    }
                }
            } else {
                if (DatabaseHelper.checkServerBan(uuid, server)) {
                    BanMessageInfo banInfo = DatabaseHelper.getBanInfo(uuid);
                    if (banInfo == null) {
                        event.getPlayer()
                            .disconnect(
                                Messages.getMessage("error-checking-ban", new ParamsBuilder()));
                    } else {
                        event.getPlayer().sendMessage(BanMessage.getChatMessage(banInfo));
                        event.setCancelled(true);
                    }
                }
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            event.getPlayer()
                .disconnect(Messages.getMessage("error-checking-ban", new ParamsBuilder()));
        }
    }

}
