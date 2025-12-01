package f25.cs157a.evergreenbank.Controllers;

import f25.cs157a.evergreenbank.Databases.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;

public class DashboardController {

    @FXML private Label checkingBalanceLabel;
    @FXML private Label savingsBalanceLabel;
    @FXML private TextField addCheckingField;
    @FXML private TextField addSavingsField;

    private double checkingBalance;
    private double savingsBalance;
    private int currentUserId;

    // Set the data from the view, this will display user info when we open the dashboard(after sign in)
    public void setData(UserRepository.AccountsView view) {
        currentUserId = view.userID; // This is the most important part of this method, allows us to show userID to other controllers
        if (checkingBalanceLabel != null) {
            checkingBalanceLabel.setText(String.format("Checking Balance: $%.2f", view.checkingBalance));
            checkingBalance = view.checkingBalance;
        }
        if (savingsBalanceLabel != null) {
            savingsBalanceLabel.setText(String.format("Savings Balance: $%.2f", view.savingsBalance));
            savingsBalance = view.savingsBalance;
        }
    }

    @FXML
    private void addMoneyToAccounts(ActionEvent event) {

        String chkText = addCheckingField.getText().trim();
        String savText = addSavingsField.getText().trim();

        double chkAmount = 0;
        double savAmount = 0;

        try {
            if (!chkText.isEmpty()) chkAmount = Double.parseDouble(chkText);
            if (!savText.isEmpty()) savAmount = Double.parseDouble(savText);
            if (chkAmount < 0 || savAmount < 0) {
                return;
            }
            UserRepository.depositToAccounts(currentUserId, chkAmount, savAmount);
            checkingBalance += chkAmount;
            savingsBalance  += savAmount;
            checkingBalanceLabel.setText(String.format("Checking Balance: $%.2f", checkingBalance));
            savingsBalanceLabel.setText(String.format("Savings Balance: $%.2f", savingsBalance));
            addCheckingField.clear();
            addSavingsField.clear();

        } catch (NumberFormatException e) {
        } catch (Exception e) {
        }
    }

    // When we click the transfer button we need to switch to the transfer scene, also set the userID in the other controller
    @FXML
    private void onTransfer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/transfer.fxml"));
        Parent userRoot = loader.load();
        TransferController controller = loader.getController();
        controller.setCurrentUserId(currentUserId);
        controller.setSavingsBalance(savingsBalance);
        controller.setCheckingBalance(checkingBalance);
        controller.loadAccountData();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(userRoot);
    }

    @FXML
    private void handleUserDeletion(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setHeaderText("Are you sure you want to delete this user?");
        confirmation.setContentText("You will permanently lose access to all your account funds.");
    
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                boolean success = UserRepository.deleteUser(currentUserId);
                if (success) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("User Deleted");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("The user has been successfully deleted.");
                    successAlert.showAndWait();

                    // Navigate back to the main view
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
                    Parent mainRoot = loader.load();
                    Scene scene = ((Node) event.getSource()).getScene();
                    scene.setRoot(mainRoot);
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to delete user.");
                errorAlert.setContentText("An error occurred while deleting the user.");
                errorAlert.showAndWait();
            }
        }
    }
   
    // This onBack sends us back to the main view, it is the top bar onBack that we have been using
    // THIS IS A TOP BAR METHOD (future grant)
    @FXML
    private void onBack(javafx.scene.input.MouseEvent e) throws java.io.IOException {
        Parent main = FXMLLoader.load(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
        ((Node)e.getSource()).getScene().setRoot(main);
    }

    
}
