package f25.cs157a.evergreenbank;

public class CheckingAccount extends Account{

    public CheckingAccount(int routingNumber, double balance) {
        super(routingNumber, balance);
    }
    @Override
    public String toString() {
        return "Checking Account[RoutingNumber=" + getRoutingNumber() + ", balance=" + getBalance() + "]";
    }
}
