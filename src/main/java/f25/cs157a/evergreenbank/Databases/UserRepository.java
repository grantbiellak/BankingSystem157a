package f25.cs157a.evergreenbank.Databases;
import f25.cs157a.evergreenbank.Classes.User;

import java.sql.*;


public final class UserRepository {

    private static final String url = "jdbc:mysql://localhost:3306/bankdb";
    private static final String user = "root";
    private static final String pass = "";

    public static int insertUser(User u) throws SQLException {
        // Open a live connection to the MySQL db, inside a try block
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {

            // We set AutoCommit to false in order to preserve atomicity in our method
            conn.setAutoCommit(false);
            // Start transaction inserting user
            try {
                String insertUser = "INSERT INTO users (full_name, email, phone) VALUES (?, ?, ?)";

                // Initialize userID for later
                int userID;

                try (PreparedStatement userPS = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    // Just fill in the question marks in insertSQL
                    userPS.setString(1, u.getFullName());
                    userPS.setString(2, u.getEmail());
                    userPS.setString(3, u.getPhoneNumber());
                    userPS.executeUpdate();

                    // Grab the userID (pk) in order to store for later
                    try (ResultSet generatedKeys = userPS.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            userID = generatedKeys.getInt(1);
                        }
                        else {
                            throw new SQLException("User creation failed");
                        }
                    }
                }

                // Set the user id in the user object
                u.setUserID(userID);

                // Insert savings and checking accounts
                String insertSavings =
                    "INSERT INTO accounts (user_id, account_type, balance, interest_rate) " +
                    "VALUES (?, 'SAVINGS', ?, ?)";
                try (PreparedStatement psSav = conn.prepareStatement(insertSavings)) {
                    // Fill in the question marks of insertSavings
                    psSav.setInt(1, u.getUserID());
                    psSav.setDouble(2, u.getSavingsAccount().getBalance());
                    psSav.setDouble(3, u.getSavingsAccount().getInterest());
                    psSav.executeUpdate();
                }

                // Interest rate is defaulted to null
                String insertChecking =
                    "INSERT INTO accounts (user_id, account_type, balance) " +
                    "VALUES (?, 'CHECKING', ?)";

                try (PreparedStatement psChk = conn.prepareStatement(insertChecking)) {
                    // Fill in the question marks of insertSavings
                    psChk.setInt(1, u.getUserID());
                    psChk.setDouble(2, u.getCheckingAccount().getBalance());
                    psChk.executeUpdate();
                }

                // Commit the transaction and return on success
                conn.commit();
                return userID;
                
            }
            catch (SQLException e) {
                // If any SQL errors occur we roll back the entire transaction
                conn.rollback();
                throw e;
            }
            finally {
                // Set autoCommit back to true since we only have this db connection
                conn.setAutoCommit(true);
            }
        }
    }

    // Both of these static nested classes are here in order to show information to the controllers
    public static class AccountsView {
        public int userID;
        public double savingsBalance;
        public double checkingBalance;
    }
    // This one specifically is just for a ui element
    public static class ActivityRecord {
        public String action;
        public double change;
        public Timestamp date;
    }


    // Fetch accounts for user with this id+name, or null if not found
    // This method is used for our log-in page
    public static AccountsView getAccounts(int userId, String fullName) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
    
            // Verify the user exists with this id+name
            boolean userExists = false;
            String userSql = "SELECT 1 FROM users WHERE id = ? AND full_name = ?";
            try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                ps.setInt(1, userId);
                ps.setString(2, fullName);
                try (ResultSet rs = ps.executeQuery()) {
                    // If one result exists in the result set a user was found
                    if (rs.next()) {
                        userExists = true;
                    }
                }
            }
            // If we do not find a user we return
            if (!userExists) {
                return null;
            }
    
            // Fetch accounts from this user
            String accSql = "SELECT account_type, balance FROM accounts WHERE user_id = ?";
            boolean hasChecking = false;
            boolean hasSavings = false;
            double checkingBalance = 0.0;
            double savingsBalance = 0.0;
            try (PreparedStatement ps = conn.prepareStatement(accSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("account_type");
                        double bal = rs.getDouble("balance");

                        // Assign dashboard vals, this is just a safe way to do it
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
            // This is probably unreachable, but it is here just in case
            if (!hasChecking || !hasSavings) {
                return null;
            }

            // This the for the dashboard
            // When we sign in this is how we get all of the information we need from a user
            AccountsView view = new AccountsView();
            view.userID = userId;
            view.checkingBalance = checkingBalance;
            view.savingsBalance  = savingsBalance;
            return view;
        }
    }

    // Our method for a transfer between users
    public static boolean transferByUserAndType(int fromUserId, String fromType,
                                                int toUserId, String toType,
                                                double amount) throws SQLException {
        // Throw a unique error for better user logging
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Since we are doing multiple different updates we set autocommit to false for atomicity
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setAutoCommit(false);
            try {
                // Make upper case to mimic CHECKING and SAVINGS
                fromType = fromType.toUpperCase();
                toType = toType.toUpperCase();
                Integer fromId = null;
                Integer toId = null;
                double fromBalance = 0.0;

                // The string for a prepared statement
                final String userStatements = "SELECT id, balance FROM accounts " +
                        "WHERE user_id = ? AND account_type = ? FOR UPDATE";

                // This statement is for the sender of the money
                try (PreparedStatement ps = conn.prepareStatement(userStatements)) {
                    ps.setInt(1, fromUserId);
                    ps.setString(2, fromType);
                    try (ResultSet rs = ps.executeQuery()) {
                        // If we don't have anything returned from the result set then we cant find the sender
                        // This is a theoretically unreachable scenario, just a safety check
                        if (!rs.next()) {
                            throw new SQLException("Source account not found");
                        }
                        fromId = rs.getInt("id");
                        fromBalance = rs.getDouble("balance");
                    }
                }

                // We use the same statement for the receiver of the money
                // This statement is a lot more practical, it checks if the receiver exists
                try (PreparedStatement ps = conn.prepareStatement(userStatements)) {
                    ps.setInt(1, toUserId);
                    ps.setString(2, toType);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Destination account not found");
                        }
                        toId = rs.getInt("id");
                    }
                }

                // More error handling
                if (fromId.equals(toId)) {
                    throw new SQLException("From and To accounts must differ");
                }
                if (fromBalance < amount) {
                    throw new SQLException("Insufficient funds");
                }

                // Two more pstatements to change the balance of the sender and receiver
                final String debitStatement = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                final String creditStatement = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
                try (PreparedStatement debit  = conn.prepareStatement(debitStatement);
                     PreparedStatement credit = conn.prepareStatement(creditStatement)) {
                    // Set the parameters of the prepared statement

                    debit.setDouble(1, amount);
                    debit.setInt(2, fromId);
                    debit.executeUpdate();

                    credit.setDouble(1, amount);
                    credit.setInt(2, toId);
                    credit.executeUpdate();
                }
                // Insert into the transaction table on a successful transaction
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO transfers (sender_id, receiver_id, amount, date, status) " +
                                "VALUES (?, ?, ?, NOW(), 'SUCCESS')")) {
                    // We use NOW() for current date time
                    ps.setInt(1, fromUserId);
                    ps.setInt(2, toUserId);
                    ps.setDouble(3, amount);
                    ps.executeUpdate();
                }

                // After success we commit and return
                conn.commit();
                return true;

            } catch (SQLException e) {
                // Rollback on sql exception
                conn.rollback();

                // Insert a failure into the transfer table
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO transfers (sender_id, receiver_id, amount, date, status) " +
                                "VALUES (?, ?, ?, NOW(), 'FAIL')")) {
                    ps.setInt(1, fromUserId);
                    ps.setInt(2, toUserId);
                    ps.setDouble(3, amount);
                    ps.executeUpdate();
                } catch (SQLException ignored) {
                    // If we somehow fail both transactions then nothing happens
                }

                throw e;

            } finally {
                // Reset autocommit for future methods
                conn.setAutoCommit(true);
            }
        }
    }

    // This is for adding money to an account (deposit)
    public static void depositToAccounts(int userId, double checkingAmount, double savingsAmount)
            throws SQLException {

        // First check if the checkingAmount and savingsAmount are greater than 0
        if (checkingAmount < 0 || savingsAmount < 0) {
            throw new IllegalArgumentException("Deposit amounts cannot be negative");
        }

        // If both are 0 no money is added, no reason to waste time
        if (checkingAmount == 0 && savingsAmount == 0) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // Multiple updates so we turn off autoCommit for atomicity
            conn.setAutoCommit(false);
            try {
                // We only need to update checking amount if the user enters more than 0
                if (checkingAmount > 0) {
                    // Prepared statement to update a specific user
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE accounts SET balance = balance + ? " +
                                    "WHERE user_id = ? AND account_type = 'CHECKING'")) {
                        ps.setDouble(1, checkingAmount);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }

                // Update savings under the same condition that we update checking
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
                // We have no need to return anything this time, the info is stored in other places
            } catch (SQLException e) {
                // Basic sql catch
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
            // Turn off auto commit, multiple tables are being changed
            try {
                // Delete accounts associated with userId
                String deleteAccounts = "DELETE FROM accounts WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteAccounts)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // Delete user from the table iteslf
                String deleteUser = "DELETE FROM users WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteUser)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                // We delete the accounts associated with user first because of the fk constraint on it
                // If we deleted from users first we would get an error

                // We have a return value so we can use an alert on the ui
                conn.commit();
                return true;

            } catch (SQLException e) {
                // Basic sql catch
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // Boolean method in order to make sure a user can or cannot take out a loan
    // If a user has a loan, then they cannot take another one out logically and so we need this method
    public static boolean hasUnpaidLoan(int userId) throws SQLException {
        String query = "SELECT 1 FROM loan WHERE customer_id = ? AND status = 'UNPAID'";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
        // We don't catch since we are not making changes, only getting data
    }
    
    // The way we request a loan
    public static boolean requestLoan(int userId, double amount) throws SQLException {
        String query = "INSERT INTO loan (customer_id, amount, date, status) VALUES (?, ?, NOW(), 'UNPAID')";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setDouble(2, amount);
            return ps.executeUpdate() > 0;
        }
    }

    // Method for paying back a loan
    public static boolean payLoan(int userId, double amount, String accountType) throws SQLException {
        String updateLoan = "UPDATE loan SET amount = amount - ? WHERE customer_id = ? AND status = 'UNPAID'";
        String updateAccount = "UPDATE accounts SET balance = balance - ? WHERE user_id = ? AND account_type = ?";
        // We take money away from the loan, and we take money away from the account

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // Multiple updates means auto commit is false
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

                // We have a return value so we can log a successful or unsuccessful payment
                conn.commit();
                return true;
    
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // If a loan gets fully paid we update it to say paid and not unpaid
    public static void markLoanAsPaid(int userId) throws SQLException {
        String query = "UPDATE loan SET status = 'PAID' WHERE customer_id = ? AND amount <= 0";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    // We want to display to the user how much they have left to pay of their loan
    public static double getUnpaidLoanAmount(int userId) throws SQLException {
        String query = "SELECT amount FROM loan WHERE customer_id = ? AND status = 'UNPAID'";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // If the user does have a loan then we just display the amount of money they have to pay
                    return rs.getDouble("amount");
                }
            }
        }
        // If the user has no loan they have 0 to pay
        return 0;
    }

    // This method is used to get the interest rate of a savings account
    public static double getSavingsInterestRate(int userId) throws SQLException {
        // Only savings accounts have an interest rate
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
        // No interest rate implies no savings account
        throw new SQLException("Savings account not found for user ID: " + userId);
    }

    // This is how you remove money from your account essentially or withdraw
    public static void withdrawFromAccounts(int userId, double checkingAmount, double savingsAmount)
        throws SQLException {

        // We cannot withdraw a negative amount of money
        if (checkingAmount < 0 || savingsAmount < 0) {
            throw new IllegalArgumentException("Withdrawal amounts cannot be negative.");
        }

        // No point in withdrawing 0 so we return quickly
        if (checkingAmount == 0 && savingsAmount == 0) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // Autocommit is set to false since we have multiple updates
            conn.setAutoCommit(false);
            try {
                // Withdraw from checking account
                if (checkingAmount > 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE accounts SET balance = balance - ? " +
                                    "WHERE user_id = ? AND account_type = 'CHECKING'")) {
                        ps.setDouble(1, checkingAmount);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }
                // Withdraw from savings account
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
                // Basic sql catch
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // This method is for the ui table on the dashboard
    public static java.util.List<ActivityRecord> getUserActivity(int userId) throws SQLException {
        java.util.List<ActivityRecord> list = new java.util.ArrayList<>();
        // Create a new list of ActivityRecords
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {

            String transferSql = "SELECT sender_id, receiver_id, amount, date, status FROM transfers" +
           " WHERE (sender_id = ? OR receiver_id = ?) AND status = 'SUCCESS' ";

            try (PreparedStatement ps = conn.prepareStatement(transferSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int senderId = rs.getInt("sender_id");
                        int receiverId = rs.getInt("receiver_id");
                        double amount = rs.getDouble("amount");
                        Timestamp date = rs.getTimestamp("date");
                        // We get date since we want to place them in date order of when they happen

                        ActivityRecord r = new ActivityRecord();
                        r.date = date;
                        // The 3 possible ways a transfer can happen
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
        // Sort by date descending
        list.sort((a, b) -> b.date.compareTo(a.date));
        return list;
    }




    private UserRepository() {
        // private constructor to prevent instantiation
    }
}
