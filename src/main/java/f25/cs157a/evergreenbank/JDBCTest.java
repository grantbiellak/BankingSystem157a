package f25.cs157a.evergreenbank;

import java.sql.*;

public class JDBCTest {
    public static void main(String[] args) {
        // 1) Load driver (optional on recent Java, but useful for clarity)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded ✔");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver not found on classpath");
            e.printStackTrace();
            return;
        }

        // 2) Connect and do a tiny query
        String url  = "jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC";
        String user = "root";        // <-- your user
        String pass = ""; // <-- your password (or "" if none)

        try (
                Connection conn = DriverManager.getConnection(url, user, pass);
                PreparedStatement ps = conn.prepareStatement("SELECT 1");
                ResultSet rs = ps.executeQuery()
        ) {
            System.out.println("Connected ✔");
            if (rs.next()) {
                System.out.println("Test query returned: " + rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("SQL error ❌: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
