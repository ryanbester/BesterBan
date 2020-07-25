package com.ryanbester.besterban.database;

import com.ryanbester.besterban.BanMessageInfo;
import com.ryanbester.besterban.BesterBan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Helper class contains methods for interfacing the database.
 */
public class DatabaseHelper {

    /**
     * Generates a unique appeal ID.
     *
     * @return The unique appeal ID.
     * @throws SQLException If a database error occurs.
     */
    public static String generateAppealID() throws SQLException {
        String appealID = "";

        boolean idFound = true;
        while (idFound) {
            appealID = generateRandomString(8);

            Connection connection = DatabaseConnection.getConnection(BesterBan.databaseOptions);
            if (connection == null) {
                return null;
            }

            String querySQL = "select count(*) as idCount from " + BesterBan.databaseOptions.table
                + " where appeal_id = ?";
            PreparedStatement stmt = connection.prepareStatement(querySQL);
            stmt.setString(1, appealID); // Appeal ID

            ResultSet rs = stmt.executeQuery();
            rs.next();
            if (rs.getInt("idCount") == 0) {
                idFound = false;
            }
        }

        return appealID;
    }

    /**
     * Generates a random alphanumeric string.
     *
     * @param length The length of the string.
     * @return The random string.
     */
    private static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = (int) (chars.length() * Math.random());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    /**
     * Adds a ban to the database.
     *
     * @param uuid     The player's UUID.
     * @param expiry   The expiry date of the ban, or null if it is permanent.
     * @param type     The ban type.
     * @param server   The server the ban applies to, or null if it is a network ban.
     * @param reason   The reason the player was banned.
     * @param appealID The appeal ID.
     * @param bannedBy Who the player was banned by.
     * @return The appeal ID, or null on error.
     * @throws SQLException If a database error occurs.
     */
    public static String addBan(String uuid, LocalDateTime expiry, String type, String server,
        String reason, String appealID, String bannedBy)
        throws SQLException {
        Connection connection = DatabaseConnection.getConnection(BesterBan.databaseOptions);
        if (connection == null) {
            return null;
        }

        String expiryStr = null;
        if (expiry != null) {
            expiryStr = expiry.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        String insertSQL = "insert into " + BesterBan.databaseOptions.table + " values("
            + "?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(insertSQL);
        stmt.setString(1, uuid); // Player UUID
        stmt.setString(2,
            LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))); // Date banned
        stmt.setString(3, expiryStr); // Expiry date
        stmt.setString(4, type); // Type
        stmt.setString(5, server); // Server
        stmt.setString(6, reason); // Reason
        stmt.setString(7, appealID); // Appeal ID
        stmt.setString(8, bannedBy); // Banned by

        stmt.executeUpdate();

        return appealID;
    }

    /**
     * Checks if a network ban exists for a player.
     *
     * @param uuid The player's UUID.
     * @return True if the player is banned, or false if they are not.
     * @throws SQLException If a database error occurs.
     */
    public static boolean checkNetworkBan(String uuid) throws SQLException {
        Connection connection = DatabaseConnection.getConnection(BesterBan.databaseOptions);
        if (connection == null) {
            return false;
        }

        String selectSQL = "select * from " + BesterBan.databaseOptions.table
            + " where uuid = ?";
        PreparedStatement stmt = connection.prepareStatement(selectSQL);
        stmt.setString(1, uuid); // Player UUID

        ResultSet rs = stmt.executeQuery();
        boolean banned = false;

        while (rs.next()) {
            if ("Network".equals(rs.getString("type"))) {
                Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
                if (expiryTimestamp == null) {
                    banned = true;
                } else {
                    LocalDateTime expiryDate = expiryTimestamp.toLocalDateTime();
                    if (!expiryDate.isBefore(LocalDateTime.now())) {
                        banned = true;
                    } else {
                        // Delete expired bans
                        removeBan(uuid);
                    }
                }
            }
        }

        return banned;
    }

    /**
     * Checks if a server ban exists for a player. Also checks for a network ban.
     *
     * @param uuid   The player's UUID.
     * @param server The server name.
     * @return True if the player is banned, or false if they are not.
     * @throws SQLException If a database error occurs.
     */
    public static boolean checkServerBan(String uuid, String server) throws SQLException {
        Connection connection = DatabaseConnection.getConnection(BesterBan.databaseOptions);
        if (connection == null) {
            return false;
        }

        // Check for network ban first
        if (checkNetworkBan(uuid)) {
            return true;
        }

        String selectSQL = "select * from " + BesterBan.databaseOptions.table
            + " where uuid = ? and server = ?";
        PreparedStatement stmt = connection.prepareStatement(selectSQL);
        stmt.setString(1, uuid); // Player UUID
        stmt.setString(2, server); // Server

        ResultSet rs = stmt.executeQuery();
        boolean banned = false;

        while (rs.next()) {
            if ("Server".equals(rs.getString("type"))) {
                Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
                if (expiryTimestamp == null) {
                    banned = true;
                } else {
                    LocalDateTime expiryDate = expiryTimestamp.toLocalDateTime();
                    if (!expiryDate.isBefore(LocalDateTime.now())) {
                        banned = true;
                    } else {
                        // Delete expired bans
                        removeBan(uuid, server);
                    }
                }
            }
        }

        return banned;
    }

    /**
     * Gets information on a ban. If the player has multiple bans, this will get the information
     * about the first ban in the database.
     *
     * @param uuid The player's UUID.
     * @return The ban information.
     * @throws SQLException If a database error occurs.
     */
    public static BanMessageInfo getBanInfo(String uuid) throws SQLException {
        Connection connection = DatabaseConnection.getConnection(BesterBan.databaseOptions);
        if (connection == null) {
            return null;
        }

        String selectSQL = "select * from " + BesterBan.databaseOptions.table
            + " where uuid = ?";
        PreparedStatement stmt = connection.prepareStatement(selectSQL);
        stmt.setString(1, uuid); // Player UUID

        ResultSet rs = stmt.executeQuery();
        BanMessageInfo info = null;

        if (rs.next()) {
            String duration = "Permanent";

            LocalDateTime bannedDate = rs.getTimestamp("date_banned").toLocalDateTime();
            Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
            if (expiryTimestamp != null) {
                LocalDateTime expiryDate = expiryTimestamp.toLocalDateTime();
                duration = BanMessageInfo.getPeriodString(Duration.between(bannedDate, expiryDate));
            }

            info = new BanMessageInfo(
                rs.getString("type"),
                duration,
                rs.getString("reason"),
                rs.getString("appeal_id")
            );
        }

        return info;
    }

    /**
     * Removes a ban from the database.
     *
     * @param uuid The player's UUID.
     * @return True on success or false on failure.
     * @throws SQLException If a database error occurs.
     */
    public static boolean removeBan(String uuid) throws SQLException {
        Connection connection = DatabaseConnection.getConnection(BesterBan.databaseOptions);
        if (connection == null) {
            return false;
        }

        String deleteSQL = "delete from " + BesterBan.databaseOptions.table
            + " where uuid = ?";
        PreparedStatement stmt = connection.prepareStatement(deleteSQL);
        stmt.setString(1, uuid); // Player UUID

        stmt.executeUpdate();

        return true;
    }

    /**
     * Removes a ban from the database.
     *
     * @param uuid   The player'\s UUID.
     * @param server The server to remove the ban for.
     * @return True on success or false on failure.
     * @throws SQLException If a database error occurs.
     */
    public static boolean removeBan(String uuid, String server) throws SQLException {
        Connection connection = DatabaseConnection.getConnection(BesterBan.databaseOptions);
        if (connection == null) {
            return false;
        }

        String deleteSQL = "delete from " + BesterBan.databaseOptions.table
            + " where uuid = ? and server = ?";
        PreparedStatement stmt = connection.prepareStatement(deleteSQL);
        stmt.setString(1, uuid); // Player UUID
        stmt.setString(2, server); // Server

        stmt.executeUpdate();

        return true;
    }

}
