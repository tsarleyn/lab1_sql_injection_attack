
package com.ias101.lab1.utils;
import com.ias101.lab1.database.util.DBUtil;
import com.ias101.lab1.model.User;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for performing CRUD operations on user data in the database.
 */
public class Crud {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/database/sample.db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{5,20}$");
    private static final Pattern VALID_PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9@#$%^&+=]{8,}$");

    // SQL queries as constants
    private static final String SELECT_ALL_USERS = "SELECT * FROM user_data";

    /**
     * Retrieves <b>all users</b> from the database.
     *
     * @return List of all User objects in the database
     * @throws RuntimeException if there is an error fetching users from database
     */
    public static List<User> getAll() {
        List<User> users = new ArrayList<>();

        try (var connection = DBUtil.connect(DB_URL, DB_USER, DB_PASSWORD);
             var stmt = connection.createStatement();
             var rs = stmt.executeQuery(SELECT_ALL_USERS)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all users", e);
        }
    }

    /**
     * Searches for a user by their <b>username</b>.
     *
     * @param username The username to search for
     * @return User object if found, null otherwise
     * @throws RuntimeException if there is an error searching the database
     */
    public static User searchByUsername(String username) {
        if (!isValidInput(username, VALID_USERNAME_PATTERN)) {
            throw new IllegalArgumentException("Invalid username format");
        }
        String query = "SELECT * FROM user_data WHERE username = '" + escapeInput(username) + "'";

        try (var connection = DBUtil.connect(DB_URL, DB_USER, DB_PASSWORD);
             var stmt = connection.createStatement();
             var rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching for user: " + username, e);
        }
        return null;
    }

    /**
     * Deletes a user from the database by their username.
     *
     * @param username The username of the user to delete
     * @throws RuntimeException if there is an error deleting the user
     */
    public static void deleteUserByUsername(String username) {
        if (!isValidInput(username, VALID_USERNAME_PATTERN)) {
            throw new IllegalArgumentException("Invalid username format");
        }
        String query = "DELETE FROM user_data WHERE username = '" + escapeInput(username) + "'";

        try (var connection = DBUtil.connect(DB_URL, DB_USER, DB_PASSWORD);
             var stmt = connection.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + username, e);
        }
    }

    /**
     * Extracts user data from a ResultSet and creates a User object.
     *
     * @param rs The ResultSet containing user data
     * @return User object created from the ResultSet data
     * @throws SQLException if there is an error reading from the ResultSet
     */
    private static User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("username"),
                rs.getString("password")
        );
    }

    /**
     * Escapes single quotes to prevent simple SQL injection attempts.
     *
     * @param input The user input to escape
     * @return The escaped input
     */
    private static String escapeInput(String input) {
        return input.replace("'", "''");
    }

    /**
     * Validates user input against a given pattern.
     *
     * @param input   The input to validate
     * @param pattern The regex pattern to use
     * @return True if input matches the pattern, false otherwise
     */
    private static boolean isValidInput(String input, Pattern pattern) {
        return input != null && pattern.matcher(input).matches();
    }
}