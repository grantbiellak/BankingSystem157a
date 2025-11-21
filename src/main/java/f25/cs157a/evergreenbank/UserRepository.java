package f25.cs157a.evergreenbank;
import java.sql.*;


public final class UserRepository {


    // a lot of formatting jus tried to replicate DatabaseInit.java style idk
    private static final String url = "jdbc:mysql://localhost:3306/bankdb";
    private static final String user = "root";
    private static final String pass = "";

    public static int insertUser(User u) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            
            conn.setAutoCommit(false);
            // start transaction inserting user
            try {
                String insertUser = "INSERT INTO users (full_name, email, phone) VALUES (?, ?, ?)";

                // make userID from generated keys
                int userID;

                try (PreparedStatement userPS = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    // just fill in the question marks in insertSQL
                    userPS.setString(1, u.getFullName());
                    userPS.setString(2, u.getEmail());
                    userPS.setString(3, u.getPhoneNumber());
                    userPS.executeUpdate();

                    try (ResultSet generatedKeys = userPS.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            userID = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating user failed, no ID obtained.");
                        }
                    }
                }

                // set the user id in the user object
                u.setUserID(userID);

                // insert savings and checking accounts
                String insertSavings =
                    "INSERT INTO accounts (user_id, account_type, routing_number, balance, interest_rate) " +
                    "VALUES (?, 'SAVINGS', ?, ?, ?)";
                try (PreparedStatement psSav = conn.prepareStatement(insertSavings)) {
                    // fill in the question marks of insertSavings
                    psSav.setInt(1, u.getUserID());
                    psSav.setInt(2, u.getSavingsAccount().getRoutingNumber());
                    psSav.setDouble(3, u.getSavingsAccount().getBalance());
                    psSav.setDouble(4, u.getSavingsAccount().getInterest());
                    psSav.executeUpdate();
                }

                String insertChecking =
                    "INSERT INTO accounts (user_id, account_type, routing_number, balance) " +
                    "VALUES (?, 'CHECKING', ?, ?)";

                try (PreparedStatement psChk = conn.prepareStatement(insertChecking)) {
                    // fill in the question marks of insertSavings
                    psChk.setInt(1, u.getUserID());
                    psChk.setInt(2, u.getCheckingAccount().getRoutingNumber());
                    psChk.setDouble(3, u.getCheckingAccount().getBalance());
                    psChk.executeUpdate();
                }

                conn.commit();
                return userID;
                
            }
            catch (SQLException e) {
                // rollback if a nuh uh happens
                conn.rollback();
                throw e;
            }
            finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // this is what the dashboard gonna show idk r static nested classes bad
    public static class AccountsView {
        public int savingsRouting;
        public int checkingRouting;
        public double savingsBalance;
        public double checkingBalance;

    }


    // fetch accounts for user with this id+name, or null if not found
    public static AccountsView getAccounts(int userId, String fullName) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
    
            // verify the user exists with this id+name
            boolean userExists = false;
            String userSql = "SELECT 1 FROM users WHERE id = ? AND full_name = ?";
            try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                ps.setInt(1, userId);
                ps.setString(2, fullName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userExists = true;
                    }
                }
            }
            if (!userExists) {
                return null;
            }
    
            // fetch accounts from this user
            String accSql = "SELECT account_type, routing_number, balance FROM accounts WHERE user_id = ?";
            boolean hasChecking = false, hasSavings = false;
            int checkingRouting = 0, savingsRouting = 0;
            double checkingBalance = 0.0, savingsBalance = 0.0;
    
            try (PreparedStatement ps = conn.prepareStatement(accSql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("account_type");
                        int routing  = rs.getInt("routing_number");
                        double bal   = rs.getDouble("balance");

                        // assign dashboard vals
                        if ("CHECKING".equals(type)) {
                            hasChecking = true;
                            checkingRouting = routing;
                            checkingBalance = bal;
                        }
                        else if ("SAVINGS".equals(type)) {
                            hasSavings = true;
                            savingsRouting = routing;
                            savingsBalance = bal;
                        }
                    }
                }
            }  

            if (!hasChecking || !hasSavings) {
                return null;
            }

            // this the for the dashboard
            AccountsView view = new AccountsView();
            view.checkingRouting = checkingRouting;
            view.checkingBalance = checkingBalance;
            view.savingsRouting  = savingsRouting;
            view.savingsBalance  = savingsBalance;
            return view;
        }
    }


    private UserRepository() {
        // private constructor to prevent instantiation
    }
}
