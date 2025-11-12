package f25.cs157a.evergreenbank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCTest {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.driver"); //This is the URL of our driver? not quite sure
        } catch (Exception e) {
            System.out.println("Unable to load driver"); //Catch a driver failure no clue what this means either
            return;
        }
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "user",
                    "password"); //Our driver should recognize its own URL
            //Statement stmt = conn.createStatement();
            //ResultSet rs = stmt.executeQuery(
            //"SELECT x FROM y"); THIS IS JUST AN EXAMPLE
            //while(rs.next()){
                //System.out.println(rs.getString("First_name");
            //}
            //rs.close()
            //stmt.close()
            //conn.close()
        } catch (SQLException se) {
            System.out.println("Unable to connect to database" + se.getMessage());
            se.printStackTrace(System.out);
        }

    }
}
