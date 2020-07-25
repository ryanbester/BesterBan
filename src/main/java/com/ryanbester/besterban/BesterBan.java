// Copyright (C) 2020 Ryan Bester
// This code is licensed under the MIT license

package com.ryanbester.besterban;

import com.google.common.io.ByteStreams;
import com.ryanbester.besterban.command.NetworkBan;
import com.ryanbester.besterban.command.ServerBan;
import com.ryanbester.besterban.command.TempNetworkBan;
import com.ryanbester.besterban.command.TempServerBan;
import com.ryanbester.besterban.command.Unban;
import com.ryanbester.besterban.database.DatabaseConnection;
import com.ryanbester.besterban.database.DatabaseOptions;
import com.ryanbester.besterban.event.PostLogin;
import com.ryanbester.besterban.event.ServerConnect;
import com.ryanbester.besterban.event.TabComplete;
import com.ryanbester.besterban.messages.Messages;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BesterBan extends Plugin {
	Configuration config;
	
	public static String fallbackServer = "lobby";
	public static DatabaseOptions databaseOptions;
	public static String bedrockPrefix = "";
	
	@Override
	public void onEnable() {
		loadConfig();
		BanMessage.loadMessage(this);
		BanMessage.loadChatMessage(this);
		BanMessage.loadBedrockMessage(this);

		Messages.loadMessagesFile(this);

		getProxy().getPluginManager().registerListener(this, new TabComplete(this));
		getProxy().getPluginManager().registerListener(this, new PostLogin(this));
		getProxy().getPluginManager().registerListener(this, new ServerConnect(this));

		getProxy().getPluginManager().registerCommand(this, new NetworkBan(this));
		getProxy().getPluginManager().registerCommand(this, new ServerBan(this));
		getProxy().getPluginManager().registerCommand(this, new TempNetworkBan(this));
		getProxy().getPluginManager().registerCommand(this, new TempServerBan(this));
		getProxy().getPluginManager().registerCommand(this, new Unban(this));;
	}
	
	void loadConfig() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
				InputStream is = getResourceAsStream("config.yml");
				OutputStream os = new FileOutputStream(configFile);
				ByteStreams.copy(is, os);
			} catch (Exception e) {
				getLogger().warning("Cannot create config file");
			}
		}

		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
		} catch (Exception e) {
			getLogger().warning("Cannot parse config file");
		}

		fallbackServer = config.getString("fallback-server");

		databaseOptions = new DatabaseOptions();
		databaseOptions.host = config.getString("database.host");
		databaseOptions.port = config.getInt("database.port");
		databaseOptions.database = config.getString("database.database");
		databaseOptions.table = config.getString("database.table");
		databaseOptions.username = config.getString("database.username");
		databaseOptions.password = config.getString("database.password");
		databaseOptions.ssl = config.getBoolean("database.ssl", true);

		bedrockPrefix = config.getString("bedrock-prefix");
	}

	@Override
	public void onDisable() {
		super.onDisable();
		DatabaseConnection.closeConnection();
	}
}
