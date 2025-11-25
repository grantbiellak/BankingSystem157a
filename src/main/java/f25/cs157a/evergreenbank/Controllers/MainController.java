package f25.cs157a.evergreenbank.Controllers;

import f25.cs157a.evergreenbank.Databases.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.SQLException;

public class MainController {


    @FXML
    private TextField userIdField;
    @FXML
    private TextField fullNameField;
    @FXML
    private Label errorLabel;

    // This is for the sign in button
    @FXML
    private void handleSignIn(ActionEvent event) throws IOException {

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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/dashboard.fxml"));
        Parent dashboardRoot = loader.load();
        DashboardController controller = loader.getController();
        controller.setData(view);

        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(dashboardRoot);
    }

    // This is what sends us to the new scene when we create a new user
    @FXML
    private void onCreateUser(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/user.fxml"));
        Parent userRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(userRoot);
    }

}
