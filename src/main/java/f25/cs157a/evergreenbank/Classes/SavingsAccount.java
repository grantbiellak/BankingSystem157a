package f25.cs157a.evergreenbank.Classes;

/**
 * SavingsAccount has an interest rate attribute in addition to the balance attribute inherited from Account.
 */
public class SavingsAccount extends Account {

    private double interest;

    // Construct a savings account with a given balance and interest rate
    public SavingsAccount(double balance, double interest) {
        super(balance);
        this.interest = interest;
    }
    public double getInterest() {
        return interest;
    }

}
