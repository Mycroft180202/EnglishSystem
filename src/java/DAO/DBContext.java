/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
 


public class DBContext {
    
    /*USE BELOW METHOD FOR YOUR DATABASE CONNECTION FOR BOTH SINGLE AND MULTILPE SQL SERVER INSTANCE(s)*/
    /*DO NOT EDIT THE BELOW METHOD, YOU MUST USE ONLY THIS ONE FOR YOUR DATABASE CONNECTION*/
     public Connection getConnection() throws Exception {
        String server = readEnvOrDefault("DB_SERVER", serverName);
        String port = readEnvOrDefault("DB_PORT", portNumber);
        String db = readEnvOrDefault("DB_NAME", dbName);
        String user = readEnvOrDefault("DB_USER", userID);
        String pass = readEnvOrDefault("DB_PASSWORD", password);

        String encrypt = readEnvOrDefault("DB_ENCRYPT", "false");
        String trustCert = readEnvOrDefault("DB_TRUST_SERVER_CERT", "true");

        String url = "jdbc:sqlserver://" + server + ":" + port
                + ";databaseName=" + db
                + ";encrypt=" + encrypt
                + ";trustServerCertificate=" + trustCert;

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(url, user, pass);
     }
    
    /*Insert your other code right after this comment*/
   
    /*Change/update information of your database connection, DO NOT change name of instance variables in this class*/
    private final String serverName = "localhost";
    private final String dbName = "EnglishCenterDB";
    private final String portNumber = "1433";
    private final String userID = "sa";
    private final String password = "123";

    private static String readEnvOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        if (value == null) return fallback;
        value = value.trim();
        return value.isEmpty() ? fallback : value;
    }
}
