package f25.cs157a.evergreenbank.Databases;

import java.sql.*;

public class DatabaseInit {

    // initialize the databases
    public static void initialize(){
        String url  = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "";
        try(Connection conn = DriverManager.getConnection(url,user,pass);
            Statement stmt = conn.createStatement()){
            // TODO Add this to method we can initialize in launcher
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS bankdb");
            stmt.executeUpdate("USE bankdb");

            String createUsers = """
                    CREATE TABLE IF NOT EXISTS users(
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        full_name VARCHAR(50),
                        email VARCHAR(50),
                        phone VARCHAR(15),
                        CONSTRAINT uq_email_phone UNIQUE (email, phone)
                        )
                    """;
            stmt.executeUpdate(createUsers);

            String createAccounts = """
                CREATE TABLE IF NOT EXISTS accounts(
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    account_type ENUM('CHECKING','SAVINGS') NOT NULL,
                    balance DOUBLE NOT NULL,
                    interest_rate DOUBLE NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """;
            stmt.executeUpdate(createAccounts);
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        initialize();
    }
}