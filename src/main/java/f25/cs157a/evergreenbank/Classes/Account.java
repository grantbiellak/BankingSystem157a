package f25.cs157a.evergreenbank.Classes;

/**
 * Base abstract class for different types of bank accounts (checkings & savings).
 * Stores balance attribute and methods for all account types.
 */
public abstract class Account {
    private double balance;

    // Accounts always start with $100 balance
    protected Account (double balance) {
        this.balance = 100;
    }

    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
   
}