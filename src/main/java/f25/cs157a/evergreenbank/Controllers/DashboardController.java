package f25.cs157a.evergreenbank.Controllers;

import f25.cs157a.evergreenbank.Databases.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class DashboardController {

    @FXML private TextField addCheckingField;
    @FXML private TextField addSavingsField;
    @FXML private Label mainType;
    @FXML private Label mainNumber;
    @FXML private Label mainBalance;
    @FXML private Label mainSub;
    @FXML private Label dropType;
    @FXML private Label dropNumber;
    @FXML private Label dropBalance;
    @FXML private Label dropSub;

    private double checkingBalance;
    private double savingsBalance;
    private int currentUserId;

    public void setData(UserRepository.AccountsView view) {
        currentUserId = view.userID;
        checkingBalance = view.checkingBalance;
        savingsBalance  = view.savingsBalance;

        mainType.setText("Checking");
        mainSub.setText("Available Balance");
        mainBalance.setText(String.format("$%.2f", checkingBalance));

        dropType.setText("Savings");
        dropSub.setText("Available Balance");
        dropBalance.setText(String.format("$%.2f", savingsBalance));
    }

    public void setData(int userId, double checking, double savings) {
        this.currentUserId    = userId;
        this.checkingBalance  = checking;
        this.savingsBalance   = savings;
        applyDataToView();
    }

    private void applyDataToView() {
        if (mainType == null || mainBalance == null ||
                dropType == null || dropBalance == null) {
            return;
        }

        mainType.setText("Checking");
        mainSub.setText("Available Balance");
        mainBalance.setText(String.format("$%.2f", checkingBalance));

        dropType.setText("Savings");
        dropSub.setText("Available Balance");
        dropBalance.setText(String.format("$%.2f", savingsBalance));

        String idText = "User " + currentUserId;
        mainNumber.setText(idText);
        dropNumber.setText(idText);
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

            mainBalance.setText(String.format("$%.2f", checkingBalance));
            dropBalance.setText(String.format("$%.2f", savingsBalance));

            addCheckingField.clear();
            addSavingsField.clear();

        } catch (NumberFormatException e) {
        } catch (Exception e) {
        }
    }

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

    @FXML
    private void onBack(javafx.scene.input.MouseEvent e) throws java.io.IOException {
        Parent main = FXMLLoader.load(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
        ((Node)e.getSource()).getScene().setRoot(main);
    }

}
