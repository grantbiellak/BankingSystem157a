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

    @FXML private TextField amountCheckingField;
    @FXML private TextField amountSavingsField;
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

        String chkText = amountCheckingField.getText().trim();
        String savText = amountSavingsField.getText().trim();

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

            amountCheckingField.clear();
            amountSavingsField.clear();

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
    private void onManageLoans(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/loan.fxml"));
        Parent userRoot = loader.load();
        LoanController controller = loader.getController();
        controller.setCurrentUserId(currentUserId);
        controller.setTotalBalance(checkingBalance + savingsBalance);
        controller.setAccountBalances(checkingBalance, savingsBalance);
        controller.loadLoanData(); // Load loan data (e.g., unpaid loan amount)
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(userRoot);
        System.out.println("button clicked");
    }

    @FXML
    private void withdrawMoneyFromAccounts(ActionEvent event) {
        String chkText = amountCheckingField.getText().trim();
        String savText = amountSavingsField.getText().trim();

        double chkAmount = 0;
        double savAmount = 0;

        try {
            if (!chkText.isEmpty()) chkAmount = Double.parseDouble(chkText);
            if (!savText.isEmpty()) savAmount = Double.parseDouble(savText);

            if (chkAmount < 0 || savAmount < 0) {
                return; // invalid
            }

            if (chkAmount > checkingBalance || savAmount > savingsBalance) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Insufficient Funds");
                alert.setHeaderText(null);
                alert.setContentText("You cannot withdraw more than the available balance.");
                alert.showAndWait();
                return;
            }

            // Update DB
            UserRepository.withdrawFromAccounts(currentUserId, chkAmount, savAmount);

            // Update local
            checkingBalance -= chkAmount;
            savingsBalance  -= savAmount;

            mainBalance.setText(String.format("$%.2f", checkingBalance));
            dropBalance.setText(String.format("$%.2f", savingsBalance));

            amountCheckingField.clear();
            amountSavingsField.clear();

        } catch (NumberFormatException e) {
            // invalid input
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    @FXML
    private void addInterestToSavings(ActionEvent event) {
        try {
            // Fetch the interest rate for the savings account from the database
            double interestRate = UserRepository.getSavingsInterestRate(currentUserId);
    
            if (interestRate <= 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Interest Rate");
                alert.setHeaderText(null);
                alert.setContentText("The interest rate for your savings account is invalid.");
                alert.showAndWait();
                return;
            }
    
            // Calculate interest
            double interestAmount = savingsBalance * interestRate;
    
            if (interestAmount <= 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Interest Added");
                alert.setHeaderText(null);
                alert.setContentText("No interest was added because the savings balance is zero.");
                alert.showAndWait();
                return;
            }
    
            // Update savings balance in the database
            UserRepository.depositToAccounts(currentUserId, 0, interestAmount);
    
            // Update local savings balance
            savingsBalance += interestAmount;
    
            // Update the UI
            dropBalance.setText(String.format("$%.2f", savingsBalance));
    
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Interest Added");
            alert.setHeaderText(null);
            alert.setContentText(String.format("Interest of $%.2f has been added to your savings account.", interestAmount));
            alert.showAndWait();
    
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while adding interest.");
            alert.showAndWait();
        }
    }

    @FXML
    private void onBack(javafx.scene.input.MouseEvent e) throws java.io.IOException {
        Parent main = FXMLLoader.load(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
        ((Node)e.getSource()).getScene().setRoot(main);
    }

}
