package f25.cs157a.evergreenbank;

public class CheckingAccount extends Account{

    public CheckingAccount(double balance) {
        super(balance);
    }
    @Override
    public String toString() {
        return "Checking Account[RoutingNumber= , balance=" + getBalance() + "]";
    }
}
