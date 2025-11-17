package f25.cs157a.evergreenbank;

public class Account {

    private int accountNumber;
    private String email;
    private String phoneNumber;
    private final SavingsAccount savingsAccount;
    private final CheckingAccount checkingAccount;
    private String fullName;

    public Account(String fullName, String email, String phoneNumber, SavingsAccount savingsAccount, CheckingAccount checkingAccount) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.savingsAccount = savingsAccount;
        this.checkingAccount = checkingAccount;
    }

    // Setters and getters in case we need them, not sure that we will
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public int getAccountNumber() {
        return this.accountNumber;
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
        return this.email + " " + this.phoneNumber + " " + this.savingsAccount.toString();
    }
}
