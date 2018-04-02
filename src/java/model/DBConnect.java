/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Norhan
 */
public class DBConnect {
    private Connection cn;
    public static Statement st;
    public DBConnect (){
        try {
            cn = SQLConnection();
            st = cn.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static Connection SQLConnection() throws SQLException {
//        System.out.println("database.SQLMicrosoft.SQLConnection()");
//        String url = "jdbc:sqlserver://deb-qat.odbc.renweb.com:1433;databaseName=deb_qat";
//        String loginName = "DEB_QAT_CUST";
//        String password = "UnderQuiet+227";
        System.out.println("database.SQLMicrosoft.SQLConnection()");
        String url = "jdbc:sqlserver://is-pan.odbc.renweb.com:1433;databaseName=is_pan";
        String loginName = "IS_PAN_CUST";
        String password = "HotelBravo+943";
        

        DriverManager.registerDriver(new SQLServerDriver());
        Connection cn = null;
        try {

            cn = DriverManager.getConnection(url, loginName, password);
        } catch (SQLException ex) {
            System.out.println("No se puede conectar con el Motor");
            System.err.println(ex.getMessage());
        }

        return cn;
    }
    
    
}
