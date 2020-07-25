package com.ryanbester.besterban.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection helper class.
 */
public class DatabaseConnection {

    private static Connection connection = null;

    /**
     * Gets the database connection object. Connects to the database if it's not already connected.
     *
     * @param options The database options to use
     * @return The connection object.
     */
    public static Connection getConnection(DatabaseOptions options) {
        if (connection == null) {
            // Create connection
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://"
                        + options.host + ":" + options.port + "/" + options.database
                        + "?autoReconnect=true" + (!options.ssl ? "&useSSL=false" : ""),
                    options.username, options.password);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return connection;
    }

    /**
     * Closes the connection to the database.
     */
    public static void closeConnection() {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
