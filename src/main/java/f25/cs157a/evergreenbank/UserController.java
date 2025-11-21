package f25.cs157a.evergreenbank;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.BorderPane;


import java.io.IOException;

import java.sql.*;

public class UserController {

    private User user;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button Submit;
    @FXML
    private DialogPane dialogPane;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label accountNumberLabel;
    @FXML
    private Label phoneNumberLabel;
    @FXML
    private Label accountFail;
    @FXML
    private Label missingField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField emailField;

    //Jawn what even is the point of this be so honest with me
    //TODO DELETE?
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
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        System.out.println("Full name: " + fullName);
        System.out.println("Email: " + email);
        String phoneNumber = phoneNumberField.getText().trim();
        System.out.println("Account Phone Number: " + phoneNumber);

        user = new User(fullName, email, phoneNumber);
        if(email.isEmpty() || phoneNumber.isEmpty() || fullName.isEmpty()) {
            missingField.setVisible(true);
            return;
        }
        try {
            int userID = UserRepository.insertUser(user);
            usernameLabel.setText("User created with name: " + fullName);
            emailLabel.setText("Email: " + email);
            phoneNumberLabel.setText("Account Phone Number: " + phoneNumber);
            accountNumberLabel.setText("Account Number: " + userID);
            Dialog<Void> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.initOwner(borderPane.getScene().getWindow());
            dialog.showAndWait();
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

    //TODO Move somewhere else (main controller when created)
    @FXML
    private void onSignIn(javafx.event.ActionEvent event) throws java.io.IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("signin.fxml"));
        Parent signInRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(signInRoot);
    }

}
