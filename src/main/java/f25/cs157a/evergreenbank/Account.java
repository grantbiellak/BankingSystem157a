package f25.cs157a.evergreenbank;

public abstract class Account {
    private int routingNumber;
    private double balance;

    protected Account (int routingNumber, double balance) {
        this.routingNumber = routingNumber;
        this.balance = balance;
    }

    public int getRoutingNumber() {
        return routingNumber;
    }
    public double getBalance() {
        return balance;
    }
    public void setRoutingNumber(int routingNumber) {
        this.routingNumber = routingNumber;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
        else {
            System.out.println("Deposit amount must be positive.");
        }
    }
    public void withdraw(double amount) {
        if (amount > 0) {
            balance -= amount;
        }
        else if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
        }
        else if(amount > balance) {
            System.out.println("Insufficient funds for withdrawal.");
        }
    }

    // idk how to check whether target is a valid account?
    public void transferFunds(Account target, double amount) {
        if (amount > 0 && amount <= this.balance) {
            this.withdraw(amount);
            target.deposit(amount);
        }
        else if (amount <= 0) {
            System.out.println("Transfer amount must be positive.");
        }
        else {
            System.out.println("Insufficient funds for transfer.");
        }
    }
}
