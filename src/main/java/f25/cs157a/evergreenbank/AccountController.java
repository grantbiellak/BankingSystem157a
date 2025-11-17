package f25.cs157a.evergreenbank;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Random;

public class AccountController {

    private Account account;
    private double SavingsBalance;
    private double interestRate;
    @FXML
    private Label balanceLabel;
    TextField accountNumberField = new TextField();
    TextField accountNameField = new TextField();


    @FXML
    protected void OnCreateAccount() {
        int accountNumber = Integer.parseInt(accountNumberField.getText());
        String accountName = accountNameField.getText();
        int routingNumber = generateRoutingNumber();
        SavingsAccount savingsAccount = new SavingsAccount(routingNumber, SavingsBalance, interestRate);
    }

    private static int generateRoutingNumber() {
        Random random = new Random();
        return 10000 + random.nextInt(90000);
    }
}
