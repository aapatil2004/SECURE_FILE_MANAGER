package server;


import java.sql.Connection;
import java.sql.DriverManager;
public class DBUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/filemanager";
    private static final String USER = "aryan";
    private static final String PASSWORD = "Sunita@40";


    public static Connection getConnection() throws Exception{

        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL,USER,PASSWORD);
    }
    
}
