package com.ucsc.mcs.impl.connector;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by JagathA on 8/11/2017.
 */
public class MySqlConnector implements SqlConnector {

    private static final MySqlConnector MYSQL_CONNECTOR = new MySqlConnector();
    private static Connection MYSQL_CONNECTION = null;

    private MySqlConnector() {
        this.loadConfig();
    }

    public static SqlConnector getInstance(){
        return MYSQL_CONNECTOR;
    }

    public boolean loadConfig(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("MySQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Connection connect(){
        if(MYSQL_CONNECTION != null){
            return MYSQL_CONNECTION;
        }

        System.out.println("-------- MySQL JDBC Connection Testing ------------");

        try {
            MYSQL_CONNECTION = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/msc","root", "root");

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            MYSQL_CONNECTION = null;
        }

        if (MYSQL_CONNECTION != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }
        return MYSQL_CONNECTION;
    }

    public boolean releaseConnection(Connection conn){
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

}
