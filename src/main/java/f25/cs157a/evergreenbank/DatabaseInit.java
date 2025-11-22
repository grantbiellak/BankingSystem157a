package f25.cs157a.evergreenbank;

import java.sql.*;

public class DatabaseInit {

    public static void initialize(){
        String url  = "jdbc:mysql://localhost:3306/";
        String user = "root";        // <-- your user
        String pass = "";

        try(Connection conn = DriverManager.getConnection(url,user,pass);
            Statement stmt = conn.createStatement()){
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
            String query = "SELECT * FROM users";
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("full_name");
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");
                    System.out.println(id + " | " + name + " | " + email + " | " + phone);
                }
            }

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

            System.out.println("Database initialized successfully.");


        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        initialize();
    }
}