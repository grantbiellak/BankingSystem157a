package f25.cs157a.evergreenbank;

public class Account {

    private int accountNumber;
    private String email;
    private int phoneNumber;
    private final SavingsAccount savingsAccount;
    private final CheckingAccount checkingAccount;

    public Account(String email, int phoneNumber, SavingsAccount savingsAccount, CheckingAccount checkingAccount) {
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
    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public int getAccountNumber() {
        return this.accountNumber;
    }
    public String getEmail() {
        return this.email;
    }
    public int getPhoneNumber() {
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
