package f25.cs157a.evergreenbank;
import java.sql.*;


public final class UserRepository {


    // a lot of formatting jus tried to replicate DatabaseInit.java style idk
    private static final String url = "jdbc:mysql://localhost:3306";
    private static final String user = "root";
    private static final String pass = "";

    public static int insertUser(User u) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, pass);
            Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("USE bankdb");
            
            conn.setAutoCommit(false);
            // start transaction inserting user
            try {
                String insertSQL = String.format("INSERT INTO users (full_name, email, phone) VALUES ('%s', '%s', '%s')",
                u.getFullName(), u.getEmail(), u.getPhoneNumber());
                
                // insert and return the generated user id
                stmt.executeUpdate(insertSQL, Statement.RETURN_GENERATED_KEYS);

                // make userID from generated keys
                int userID;
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userID = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }

                // set the user id in the user object
                u.setUserID(userID);

                // insert savings and checking accounts
                String insertSavings = String.format(
                    "INSERT INTO accounts (user_id, account_type, routing_number, balance, interest_rate) " +
                    "VALUES (%d, 'SAVINGS', %d, %f, %f)",
                    u.getUserID(), u.getSavingsAccount().getRoutingNumber(), u.getSavingsAccount().getBalance(), u.getSavingsAccount().getInterest());
                stmt.executeUpdate(insertSavings);

                String insertChecking = String.format(
                    "INSERT INTO accounts (user_id, account_type, routing_number, balance) " +
                    "VALUES (%d, 'CHECKING', %d, %f)",
                    u.getUserID(), u.getCheckingAccount().getRoutingNumber(), u.getCheckingAccount().getBalance());
                stmt.executeUpdate(insertChecking);

                conn.commit();
                return userID;
                
            }
            catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private UserRepository() {
        // private constructor to prevent instantiation
    }
}
