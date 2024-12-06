package com.uet.libraryManagement;

import java.sql.*;

public class ConnectJDBC {

    private static final String DB_URL = "jdbc:mysql://your_database";
    private static final String DB_USERNAME = "your_username";
    private static final String DB_PASSWORD = "your_password";
    private static Connection connection;

    static {
        try {

            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Kết nối thành công tới AWS RDS!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi kết nối tới cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public static void connect() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
                System.out.println("Kết nối thành công tới AWS RDS!");
            } catch (SQLException e) {
                System.err.println("Lỗi khi kết nối tới cơ sở dữ liệu: " + e.getMessage());
            }
        }
    }

    public static ResultSet executeQuery(String query) {
        Statement statement;
        ResultSet rs;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(query);
            System.out.println("executeQuery: " + query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rs;
    }

    public static ResultSet executeQueryWithParams(String query, Object... params) {
        PreparedStatement statement;
        ResultSet rs;
        try {
            statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            System.out.println("Executing prepared statement: " + statement.toString());
            rs = statement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rs;
    }

    public static void executeUpdate(String query, Object... params) {
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            System.out.println("UPDATE: " + query);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
