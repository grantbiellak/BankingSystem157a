package f25.cs157a.evergreenbank;

public class CheckingAccount {

    private int routingNumber;
    private double balance;
    public CheckingAccount(int routingNumber, double balance) {
        this.routingNumber = routingNumber;
        this.balance = 500;
    }
    public int getRoutingNumber() {
        return this.routingNumber;
    }
    public double getBalance() {
        return this.balance;
    }
    public void setRoutingNumber(int routingNumber) {
        this.routingNumber = routingNumber;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public void deposit(double amount) {
        this.balance += amount;
    }
    public void withdraw(double amount) {
        this.balance -= amount;
    }
    public void transferFunds(double amount) {
        this.balance -= amount;
    }
    @Override
    public String toString() {
        return "Checking Account[RoutingNumber=" + this.routingNumber + ", balance=" + this.balance + "]";
    }
}
