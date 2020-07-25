package com.ryanbester.besterban;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Utility classes for a player.
 */
public class PlayerUtil {

    /**
     * Checks if a username is a Bedrock username. This is determined with the Bedrock prefix.
     *
     * @param username The username.
     * @return True if the username is a Bedrock username, false if it is a Java username.
     */
    public static boolean isBedrock(String username) {
        return username.startsWith(BesterBan.bedrockPrefix);
    }

    /**
     * Gets the UUID of a username. This will only work for Java usernames.
     *
     * @param username The username.
     * @return The UUID string, or null on error or if the username is a Bedrock username.
     */
    public static String getUUID(String username) {
        if (!isBedrock(username)) {
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    return null;
                }

                DataInputStream is = new DataInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuffer data = new StringBuffer();
                String str;
                while ((str = reader.readLine()) != null) {
                    data.append(str);
                }
                is.close();

                // Parse the JSON
                JsonParser parser = new JsonParser();
                JsonElement json = parser.parse(data.toString());

                if (json.isJsonObject()) {
                    JsonObject jsonObject = json.getAsJsonObject();

                    JsonElement id = jsonObject.get("id");
                    if (id.isJsonPrimitive()) {
                        String uuid = id.getAsJsonPrimitive().getAsString();
                        return uuid;
                    }
                }

                return null;
            } catch (IOException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets the UUID from an XUID, used for Bedrock players.
     *
     * @param xuid The XUID.
     * @return The UUID string.
     */
    public static String getUUIDFromXUID(String xuid) {
        return "0000000000000000" + xuid.toLowerCase();
    }
}
