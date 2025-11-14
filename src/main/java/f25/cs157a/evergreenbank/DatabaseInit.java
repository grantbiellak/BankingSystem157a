package f25.cs157a.evergreenbank;

import java.sql.*;

public class DatabaseInit {

    public static void initialize(){
        String url  = "jdbc:mysql://localhost:3306/";
        String user = "root";        // <-- your user
        String pass = ""; // <-- your password (or "" if none)

        try(Connection conn = DriverManager.getConnection(url,user,pass);
            Statement stmt = conn.createStatement()){
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS bankdb");
            stmt.executeUpdate("USE bankdb");
            stmt.executeUpdate("DROP TABLE IF EXISTS users");
            String createUsers = """
                    CREATE TABLE IF NOT EXISTS users(
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        full_name VARCHAR(50),
                        email VARCHAR(50) UNIQUE,
                        phone VARCHAR(15)
                        )
                    """;
            stmt.executeUpdate(createUsers);
            String testAdd = """
                    INSERT INTO users(full_name, email, phone) 
                    VALUES ('Grant Biellak', 'gbiellak@gmail.com', '4086633526')""";
            stmt.executeUpdate(testAdd);

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
