package f25.cs157a.evergreenbank;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;


import java.io.IOException;
import java.util.Random;

public class AccountController {

    //Ok we have account creation here
    private Account account;
    private double SavingsBalance;
    private double interestRate;
    private double checkingBalance;

    //Labels for future use (NOT IMPLEMENTED)
    @FXML
    private Label savingsBalanceLabel;
    @FXML
    private Label checkingBalanceLabel;

    //This is the Create account button on the main screen the one that we first see on launch
    @FXML
    Button createNewAccountButton = new Button("Create New Account");

    //FUTURE USE
    @FXML
    private Label interestRateLabel;

    //Text fields for getting user information on account creation
    @FXML
    TextField fullNameField = new TextField();
    @FXML
    TextField accountPhoneNumberField = new TextField();
    @FXML
    TextField emailField = new TextField();


    //When we click the first account creation button this is what runs
    //This is the standard for every scene, notice how we are replacing the content
    //WE DO NOT CREATE A NEW SCENE, JUST DO IT ON TOP OF IT
    @FXML
    private void onCreateAccount(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("account.fxml"));
        Parent accountRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(accountRoot);
    }

    //This is what creates the Account object which we store user information in
    @FXML
    protected void handleAccountCreation() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        System.out.println("Full name: " + fullName);
        System.out.println("Email: " + email);
        String accountPhoneNumber = accountPhoneNumberField.getText();
        System.out.println("Account Phone Number: " + accountPhoneNumber);
        int savingsRoutingNumber = generateRoutingNumber();
        SavingsAccount savingsAccount = new SavingsAccount(savingsRoutingNumber, SavingsBalance, interestRate);
        int checkingRoutingNumber = generateRoutingNumber();
        CheckingAccount  checkingAccount = new CheckingAccount(checkingRoutingNumber, checkingBalance);
        account = new Account(fullName, email, accountPhoneNumber, savingsAccount, checkingAccount);


    }

    //This method should be moved somewhere else Ahmad would NOT be happy about this one.
    //This should also be changed to being a random selection from a preset data structure
    //Lazy implementation L grant
    private static int generateRoutingNumber() {
        Random random = new Random();
        return 10000 + random.nextInt(90000);
    }
}
