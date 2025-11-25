package f25.cs157a.evergreenbank.Controllers;

import f25.cs157a.evergreenbank.Classes.User;
import f25.cs157a.evergreenbank.Databases.UserRepository;
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
    @FXML private BorderPane borderPane;
    @FXML private Button Submit;
    @FXML private DialogPane dialogPane;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label accountNumberLabel;
    @FXML private Label phoneNumberLabel;
    @FXML private Label accountFail;
    @FXML private Label missingField;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField emailField;


    // THIS IS A TOP BAR METHOD TO BE CHANGED
    @FXML
    private void onBack(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
        Parent mainRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainRoot);
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
            // TODO Spruce up the look of this dialog box
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

}
