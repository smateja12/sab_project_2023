/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mateja
 */
public class sm190270_DB {
    
    private static final String username = "sa_new";
    private static final String password = "123";
    private static final String database = "sab_proj_23_db";
    private static final int port = 1433;
    private static final String server = "localhost";
    
    private static final String connectionUrl = ""
            + "jdbc:sqlserver://"+server+":"+port+";encrypt=true;"+"trustServerCertificate=true;"+"databaseName="+database;
    
    private Connection connection;
    
    private sm190270_DB() {
        try {
            connection = DriverManager.getConnection(connectionUrl, username, password);
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    private static sm190270_DB db = null;
    public static sm190270_DB getInstance() {
        if (db == null) {
            db = new sm190270_DB();
        }
        return db;
    }
    
}
