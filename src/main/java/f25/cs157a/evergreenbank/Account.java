package f25.cs157a.evergreenbank;

public class Account {

    String fullName;
    int accountNumber;
    double balance;

    public Account(String fullName, int accountNumber, double balance) {
        this.fullName = fullName;
    }

    private void setBalance(double balance) {
        this.balance = balance;
    }

    private double getBalance() {
        return this.balance;
    }
}
