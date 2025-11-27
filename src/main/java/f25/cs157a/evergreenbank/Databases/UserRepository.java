package f25.cs157a.evergreenbank.Databases;
import f25.cs157a.evergreenbank.Classes.User;

import java.sql.*;


public final class UserRepository {

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
                    "INSERT INTO accounts (user_id, account_type, balance, interest_rate) " +
                    "VALUES (?, 'SAVINGS', ?, ?)";
                try (PreparedStatement psSav = conn.prepareStatement(insertSavings)) {
                    // fill in the question marks of insertSavings
                    psSav.setInt(1, u.getUserID());
                    psSav.setDouble(2, u.getSavingsAccount().getBalance());
                    psSav.setDouble(3, u.getSavingsAccount().getInterest());
                    psSav.executeUpdate();
                }

                String insertChecking =
                    "INSERT INTO accounts (user_id, account_type, balance) " +
                    "VALUES (?, 'CHECKING', ?)";

                try (PreparedStatement psChk = conn.prepareStatement(insertChecking)) {
                    // fill in the question marks of insertSavings
                    psChk.setInt(1, u.getUserID());
                    psChk.setDouble(2, u.getCheckingAccount().getBalance());
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
        public int userID;
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
            String accSql = "SELECT account_type, balance FROM accounts WHERE user_id = ?";
            boolean hasChecking = false, hasSavings = false;
            double checkingBalance = 0.0, savingsBalance = 0.0;
    
            try (PreparedStatement ps = conn.prepareStatement(accSql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("account_type");
                        double bal   = rs.getDouble("balance");

                        // assign dashboard vals
                        if ("CHECKING".equals(type)) {
                            hasChecking = true;
                            checkingBalance = bal;
                        }
                        else if ("SAVINGS".equals(type)) {
                            hasSavings = true;
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
            view.userID = userId;
            view.checkingBalance = checkingBalance;
            view.savingsBalance  = savingsBalance;
            return view;
        }
    }

    public static boolean transferByUserAndType(int fromUserId, String fromType,
                                                int toUserId,   String toType,
                                                double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setAutoCommit(false);
            try {
                fromType = fromType.toUpperCase();
                toType   = toType.toUpperCase();

                Integer fromId = null, toId = null;
                double fromBalance = 0.0;

                final String lockByUserType = "SELECT id, balance FROM accounts " +
                        "WHERE user_id = ? AND account_type = ? FOR UPDATE";

                try (PreparedStatement ps = conn.prepareStatement(lockByUserType)) {
                    ps.setInt(1, fromUserId);
                    ps.setString(2, fromType);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Source account not found");
                        fromId = rs.getInt("id");
                        fromBalance = rs.getDouble("balance");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(lockByUserType)) {
                    ps.setInt(1, toUserId);
                    ps.setString(2, toType);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Destination account not found");
                        toId = rs.getInt("id");
                    }
                }

                if (fromId.equals(toId)) throw new SQLException("From and To accounts must differ");
                if (fromBalance < amount) throw new SQLException("Insufficient funds");

                try (PreparedStatement debit  = conn.prepareStatement(
                        "UPDATE accounts SET balance = balance - ? WHERE id = ?");
                     PreparedStatement credit = conn.prepareStatement(
                             "UPDATE accounts SET balance = balance + ? WHERE id = ?");
                ) {
                    debit.setDouble(1, amount);
                    debit.setInt(2, fromId);
                    debit.executeUpdate();

                    credit.setDouble(1, amount);
                    credit.setInt(2, toId);
                    credit.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO transfers (sender_id, receiver_id, amount, date, status) " +
                                "VALUES (?, ?, ?, NOW(), 'SUCCESS')")) {
                    ps.setInt(1, fromUserId);
                    ps.setInt(2, toUserId);
                    ps.setDouble(3, amount);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {

                conn.rollback();

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO transfers (sender_id, receiver_id, amount, date, status) " +
                                "VALUES (?, ?, ?, NOW(), 'FAIL')")) {
                    ps.setInt(1, fromUserId);
                    ps.setInt(2, toUserId);
                    ps.setDouble(3, amount);
                    ps.executeUpdate();
                } catch (SQLException ignored) {
                }

                throw e;

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }


    private UserRepository() {
        // private constructor to prevent instantiation
    }
}
