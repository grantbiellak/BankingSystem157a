package f25.cs157a.evergreenbank;

public class SavingsAccount implements AccountInt{

    String fullName;
    String email;
    int accountNumber;
    int phoneNumber;
    double balance;

    public SavingsAccount(String fullName, String email, int phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public double showBalance() {
        return balance;
    }

    @Override
    public int showAccountNumber() {
        return accountNumber;
    }

    @Override
    public String showEmail() {
        return email;
    }

    @Override
    public void setAccountNumber(int accountNumber){
        this.accountNumber = accountNumber;
    }

    @Override
    public void setBalance(double balance) {
        this.balance = balance;
    }

}
