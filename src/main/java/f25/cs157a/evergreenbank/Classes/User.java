package f25.cs157a.evergreenbank.Classes;

/**
 * User class represents a bank user with personal details and associated bank accounts.
 */
public class User {

    private int userID;
    private String fullName;
    private String email;
    private String phoneNumber;

    // Each user has exactly one savings and one checking account
    private final SavingsAccount savingsAccount;
    private final CheckingAccount checkingAccount;


    // Construct a user with fullName, email, and phoneNumber + associated accounts
    public User(String fullName, String email, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;

        this.savingsAccount = new SavingsAccount(0.0, 0.01);
        this.checkingAccount = new CheckingAccount(0.0);
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
    public int getUserID() {
        return this.userID;
    }
    public String getFullName() {
        return this.fullName;
    }
    public String getEmail() {
        return this.email;
    }
    public String getPhoneNumber() {
        return this.phoneNumber;
    }
    public SavingsAccount getSavingsAccount() {
        return this.savingsAccount;
    }
    public CheckingAccount getCheckingAccount() {
        return this.checkingAccount;
    }

}
