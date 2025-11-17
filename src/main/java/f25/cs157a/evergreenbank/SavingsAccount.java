package f25.cs157a.evergreenbank;

import javafx.application.Application;

public class SavingsAccount {

    private int routingNumber;
    private double balance;
    private double interest;

    public SavingsAccount(int routingNumber, double balance, double interest) {
        this.routingNumber = routingNumber;
        this.balance = 1000;
        this.interest = 0.01;
    }
    public int getRoutingNumber() {
        return routingNumber;
    }
    public void setRoutingNumber(int routingNumber) {
        this.routingNumber = routingNumber;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(int balance) {
        this.balance = balance;
    }
    public double getInterest() {
        return interest;
    }
    public void setInterest(double interest) {
        this.interest = interest;
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
    public void accrueInterest() {
        this.balance = balance + balance*interest;
    }

    @Override
    public String toString() {
        return "Savings account[Routing number: " + routingNumber + ", Balance: " + balance + ", Interest rate: " + interest + "]";
    }

}
