package com.ryanbester.besterban.event;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class TabComplete implements Listener {

    private Plugin plugin;

    private final List<String> durationSuggestions = Arrays
        .asList("30s", "1m", "5m", "1h", "12h", "1d", "30d", "1y");

    public TabComplete(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (event.getCursor().startsWith("/networkban")
            || event.getCursor().startsWith("/serverban")
            || event.getCursor().startsWith("/tempnetworkban")
            || event.getCursor().startsWith("/tempserverban")
            || event.getCursor().startsWith("/unban")) {

            String[] params = event.getCursor().split(" ");
            int index = params.length - 1;
            if (event.getCursor().endsWith(" ")) {
                index++;
            }

            List<String> suggestions = event.getSuggestions();

            switch (index) {
                case 1:
                    for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                        if (params.length == index) {
                            suggestions.add(player.getName());
                        } else if (player.getName().toLowerCase()
                            .startsWith(params[index].toLowerCase())) {
                            suggestions.add(player.getName());
                        }
                    }
                    break;
                case 2:
                    if (event.getCursor().startsWith("/serverban")
                        || event.getCursor().startsWith("/tempserverban")
                        || event.getCursor().startsWith("/unban")) {

                        for (Entry<String, ServerInfo> server : plugin.getProxy().getServers()
                            .entrySet()) {
                            if (params.length == index) {
                                suggestions.add(server.getKey());
                            } else if (server.getKey().toLowerCase()
                                .startsWith(params[index].toLowerCase())) {
                                suggestions.add(server.getKey());
                            }
                        }
                    } else if (event.getCursor().startsWith("/tempnetworkban")) {
                        for (String duration : durationSuggestions) {
                            if (params.length == index) {
                                suggestions.add(duration);
                            } else if (duration.toLowerCase()
                                .startsWith(params[index].toLowerCase())) {
                                suggestions.add(duration);
                            }
                        }
                    }
                    break;
                case 3:
                    if (event.getCursor().startsWith("/tempserverban")) {
                        for (String duration : durationSuggestions) {
                            if (params.length == index) {
                                suggestions.add(duration);
                            } else if (duration.toLowerCase()
                                .startsWith(params[index].toLowerCase())) {
                                suggestions.add(duration);
                            }
                        }
                    }
                default:
                    break;
            }
        }
    }

}
