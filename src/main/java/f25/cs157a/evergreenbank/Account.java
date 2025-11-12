package f25.cs157a.evergreenbank;

public class Account {

    String fullName;
    String email;
    int accountNumber;
    double balance;

    public Account(String fullName) {
        this.fullName = fullName;
    }

    private void setBalance(double balance) {
        this.balance = balance;
    }

    private double getBalance() {
        return this.balance;
    }

    private void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
    private void setEmail(String email) {
        this.email = email;
    }
    private void setFullName(String fullName) {
        this.fullName = fullName;
    }
    private void setBalance(int balance) {
        this.balance = balance;
    }
}
