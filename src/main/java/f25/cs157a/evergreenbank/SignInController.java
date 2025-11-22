package f25.cs157a.evergreenbank;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class SignInController {
    
    @FXML
    private TextField userIdField;
    @FXML
    private TextField fullNameField;
    @FXML
    private Label errorLabel;

    // for sign in button
    @FXML
    private void handleSignIn(ActionEvent event) throws IOException {

        // clear prev error msgs idk
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        // read inputs as strings
        String idText = userIdField.getText().trim();
        String nameText = fullNameField.getText().trim();
        if (idText.isEmpty() || nameText.isEmpty()) {
            if (errorLabel != null) {
                errorLabel.setText("Please enter both User ID and Full Name.");
                errorLabel.setVisible(true);
            }
            return;
        }

        // convert id to int
        int userID;
        try {
            userID = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            if (errorLabel != null) {
                errorLabel.setText("User ID must be a number.");
                errorLabel.setVisible(true);
            }
            return;
        }

        // user repo has method to find matching user to accounts idk if better location
        UserRepository.AccountsView view = null;
        try {
            view = UserRepository.getAccounts(userID, nameText);
        } catch (SQLException e) {
            e.printStackTrace();
            if (errorLabel != null) {
                errorLabel.setText("Error accessing the database.");
                errorLabel.setVisible(true);
            }
        }
        if (view == null) {
            if (errorLabel != null) {
                errorLabel.setText("Invalid User ID or Full Name.");
                errorLabel.setVisible(true);
            }
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        Parent dashboardRoot = loader.load();
        DashboardController controller = loader.getController();
        controller.setData(nameText, view);

        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(dashboardRoot);
    }


    @FXML
    private void onBack(javafx.scene.input.MouseEvent e) throws IOException {
        Parent main = FXMLLoader.load(getClass().getResource("main-view.fxml"));
        ((Node)e.getSource()).getScene().setRoot(main);
    }


}
