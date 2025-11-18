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

public class UserController {

    //Ok we have user creation here
    private User user;
    // private double SavingsBalance;
    // private double interestRate;
    // private double checkingBalance;

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
    TextField phoneNumberField = new TextField();
    @FXML
    TextField emailField = new TextField();


    //When we click the first account creation button this is what runs
    //This is the standard for every scene, notice how we are replacing the content
    //WE DO NOT CREATE A NEW SCENE, JUST DO IT ON TOP OF IT
    @FXML
    private void onCreateUser(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("user.fxml"));
        Parent userRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(userRoot);
    }

    //This is what creates the user object which we store user information in
    @FXML
    protected void handleUserCreation() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        System.out.println("Full name: " + fullName);
        System.out.println("Email: " + email);
        String phoneNumber = phoneNumberField.getText();
        System.out.println("Account Phone Number: " + phoneNumber);

        user = new User(fullName, email, phoneNumber);
    }

}
