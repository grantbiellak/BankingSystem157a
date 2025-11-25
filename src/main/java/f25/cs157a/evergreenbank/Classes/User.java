package f25.cs157a.evergreenbank.Classes;

public class User {

    private int userID;
    private String fullName;
    private String email;
    private String phoneNumber;
    private final SavingsAccount savingsAccount;
    private final CheckingAccount checkingAccount;

    public User(String fullName, String email, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;

        this.savingsAccount = new SavingsAccount(0.0, 0.01);
        this.checkingAccount = new CheckingAccount(0.0);
    }

    // Setters and getters in case we need them, not sure that we will
    public void setUserID(int userID) {
        this.userID = userID;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    @Override
    public String toString(){
        return this.email + " " + this.phoneNumber + " " + this.savingsAccount.toString() + " " + this.checkingAccount.toString();
    }
}
