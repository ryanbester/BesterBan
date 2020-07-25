package com.ryanbester.besterban.event;

import com.ryanbester.besterban.BanMessage;
import com.ryanbester.besterban.BanMessageInfo;
import com.ryanbester.besterban.PlayerUtil;
import com.ryanbester.besterban.database.DatabaseHelper;
import com.ryanbester.besterban.messages.Messages;
import com.ryanbester.besterban.messages.ParamsBuilder;
import java.sql.SQLException;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PostLogin implements Listener {

    private Plugin plugin;

    public PostLogin(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        try {
            String uuid = event.getPlayer().getUniqueId().toString().replaceAll("-", "");
            String username = event.getPlayer().getName();

            if (DatabaseHelper.checkNetworkBan(uuid)) {
                BanMessageInfo banInfo = DatabaseHelper.getBanInfo(uuid);
                if (banInfo == null) {
                    event.getPlayer()
                        .disconnect(Messages.getMessage("error-checking-ban", new ParamsBuilder()));
                } else {
                    event.getPlayer()
                        .disconnect(BanMessage.getMessage(banInfo, PlayerUtil.isBedrock(username)));
                }
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            event.getPlayer()
                .disconnect(Messages.getMessage("error-checking-ban", new ParamsBuilder()));
        }
    }

}
