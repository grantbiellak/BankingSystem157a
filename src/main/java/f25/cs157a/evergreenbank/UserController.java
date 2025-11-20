package f25.cs157a.evergreenbank;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;


import java.io.IOException;

import java.sql.*;

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
    private Button Submit;

    //FUTURE USE
    @FXML
    private Label interestRateLabel;

    @FXML
    private Label accountFail;

    @FXML
    private Label missingField;
    //Text fields for getting user information on account creation
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField emailField;

    @FXML
    private void initialize() {
        System.out.println("inject fullNameField=" + fullNameField);
        System.out.println("inject emailField=" + emailField);
        System.out.println("inject phoneNumberField=" + phoneNumberField);
        System.out.println("controller @" + System.identityHashCode(this));
    }



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

    @FXML
    private void onBack(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent mainRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainRoot);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
    }

    //This is what creates the user object which we store user information in
    @FXML
    protected void handleUserCreation() {
        missingField.setVisible(false);
        accountFail.setVisible(false);
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        System.out.println("Full name: " + fullName);
        System.out.println("Email: " + email);
        String phoneNumber = phoneNumberField.getText();
        System.out.println("Account Phone Number: " + phoneNumber);

        user = new User(fullName, email, phoneNumber);
        if(email.isEmpty() || phoneNumber.isEmpty() || fullName.isEmpty()) {
            missingField.setVisible(true);
            return;
        }
        try {
            int userID = UserRepository.insertUser(user);
            System.out.println("User created with ID: " + userID);

            fullNameField.clear();
            emailField.clear();
            phoneNumberField.clear();
        }
        catch (java.sql.SQLIntegrityConstraintViolationException e) {
            accountFail.setVisible(true);
            System.out.println("A user with this email already exists.");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error creating user.");
        }
    }

}
