package f25.cs157a.evergreenbank;

public class SavingsAccount extends Account{

    private double interest;

    public SavingsAccount(double balance, double interest) {
        super(balance);
        this.interest = interest;
    }
    public double getInterest() {
        return interest;
    }
    public void setInterest(double interest) {
        this.interest = interest;
    }
    public void accrueInterest() {
       setBalance(getBalance() + (getBalance() * interest));
    }

    @Override
    public String toString() {
        return "Savings account[Routing number: Balance: " + getBalance() + ", Interest rate: " + interest + "]";

    }

}
