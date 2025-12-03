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

    public static class ActivityRecord {
        public String action;
        public double change;
        public Timestamp date;
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

    public static void depositToAccounts(int userId, double checkingAmount, double savingsAmount)
            throws SQLException {

        if (checkingAmount < 0 || savingsAmount < 0) {
            throw new IllegalArgumentException("Deposit amounts cannot be negative.");
        }

        if (checkingAmount == 0 && savingsAmount == 0) {
            return;
        }

        final String url = UserRepository.url;
        final String user = UserRepository.user;
        final String pass = UserRepository.pass;

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {

            conn.setAutoCommit(false);
            try {
                if (checkingAmount > 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE accounts SET balance = balance + ? " +
                                    "WHERE user_id = ? AND account_type = 'CHECKING'")) {
                        ps.setDouble(1, checkingAmount);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }

                // Update savings
                if (savingsAmount > 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE accounts SET balance = balance + ? " +
                                    "WHERE user_id = ? AND account_type = 'SAVINGS'")) {
                        ps.setDouble(1, savingsAmount);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static boolean deleteUser(int userId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setAutoCommit(false);

            try {
                // Delete accounts associated with user
                String deleteAccounts = "DELETE FROM accounts WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteAccounts)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // Delete user
                String deleteUser = "DELETE FROM users WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteUser)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static boolean hasUnpaidLoan(int userId) throws SQLException {
        String query = "SELECT 1 FROM loan WHERE customer_id = ? AND status = 'UNPAID'";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    

        public static boolean requestLoan(int userId, double amount) throws SQLException {
        String query = "INSERT INTO loan (customer_id, amount, date, status) VALUES (?, ?, NOW(), 'UNPAID')";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setDouble(2, amount);
            return ps.executeUpdate() > 0;
        }
    }
    
    public static boolean payLoan(int userId, double amount, String accountType) throws SQLException {
        String updateLoan = "UPDATE loan SET amount = amount - ? WHERE customer_id = ? AND status = 'UNPAID'";
        String updateAccount = "UPDATE accounts SET balance = balance - ? WHERE user_id = ? AND account_type = ?";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setAutoCommit(false);
            try (PreparedStatement loanPS = conn.prepareStatement(updateLoan);
                 PreparedStatement accountPS = conn.prepareStatement(updateAccount)) {
    
                loanPS.setDouble(1, amount);
                loanPS.setInt(2, userId);
                loanPS.executeUpdate();
    
                accountPS.setDouble(1, amount);
                accountPS.setInt(2, userId);
                accountPS.setString(3, accountType.toUpperCase());
                accountPS.executeUpdate();
    
                conn.commit();
                return true;
    
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    public static void markLoanAsPaid(int userId) throws SQLException {
        String query = "UPDATE loan SET status = 'PAID' WHERE customer_id = ? AND amount <= 0";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
    
    public static double getUnpaidLoanAmount(int userId) throws SQLException {
        String query = "SELECT amount FROM loan WHERE customer_id = ? AND status = 'UNPAID'";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("amount");
                }
            }
        }
        return 0;
    }

    public static double getSavingsInterestRate(int userId) throws SQLException {
        String query = "SELECT interest_rate FROM accounts WHERE user_id = ? AND account_type = 'SAVINGS'";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("interest_rate");
                }
            }
        }
        throw new SQLException("Savings account not found for user ID: " + userId);
    }

    public static void withdrawFromAccounts(int userId, double checkingAmount, double savingsAmount)
        throws SQLException {

        if (checkingAmount < 0 || savingsAmount < 0) {
            throw new IllegalArgumentException("Withdrawal amounts cannot be negative.");
        }

        if (checkingAmount == 0 && savingsAmount == 0) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setAutoCommit(false);
            try {
                // Withdraw from CHECKING
                if (checkingAmount > 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE accounts SET balance = balance - ? " +
                                    "WHERE user_id = ? AND account_type = 'CHECKING'")) {
                        ps.setDouble(1, checkingAmount);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }

                // Withdraw from SAVINGS
                if (savingsAmount > 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE accounts SET balance = balance - ? " +
                                    "WHERE user_id = ? AND account_type = 'SAVINGS'")) {
                        ps.setDouble(1, savingsAmount);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static java.util.List<ActivityRecord> getUserActivity(int userId) throws SQLException {
        java.util.List<ActivityRecord> list = new java.util.ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {

            String transferSql = """
            SELECT sender_id, receiver_id, amount, date, status
            FROM transfers
            WHERE (sender_id = ? OR receiver_id = ?)
              AND status = 'SUCCESS'
        """;

            try (PreparedStatement ps = conn.prepareStatement(transferSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int senderId   = rs.getInt("sender_id");
                        int receiverId = rs.getInt("receiver_id");
                        double amount  = rs.getDouble("amount");
                        Timestamp date = rs.getTimestamp("date");

                        ActivityRecord r = new ActivityRecord();
                        r.date = date;

                        if (senderId == userId && receiverId == userId) {
                            r.action = "Transfer between your accounts";
                            r.change = 0.0;
                        } else if (senderId == userId) {
                            r.action = "Transfer to User " + receiverId;
                            r.change = -amount;
                        } else {
                            r.action = "Transfer from User " + senderId;
                            r.change = +amount;
                        }

                        list.add(r);
                    }
                }
            }
        }

        // Sort by date descending (most recent first)
        list.sort((a, b) -> b.date.compareTo(a.date));

        return list;
    }




    private UserRepository() {
        // private constructor to prevent instantiation
    }
}
