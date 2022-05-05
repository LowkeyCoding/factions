package io.icker.factions.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.icker.factions.FactionsMod;

public class Database {
    public static Connection connection;

    public static void connect(String url) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            connection = DriverManager.getConnection(url);
            FactionsMod.LOGGER.info("Successfully connected to database");
        } catch (SQLException e) {
            FactionsMod.LOGGER.error("Error disconnecting from database");
        }
    }

    public static void disconnect() {
        try {
            connection.close();
            FactionsMod.LOGGER.info("Successfully disconnected from database");
        } catch (SQLException e) {
            FactionsMod.LOGGER.error("Error disconnecting from database");
        }
    }
}
