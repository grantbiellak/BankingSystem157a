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
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the user dashboard view.
 * Displays account balances and recent activity.
 * Allows navigation to transfer and loan management views.
 * Handles deposits, withdrawals, and interest addition.
 */
public class DashboardController {

    @FXML private Label mainType;
    @FXML private Label mainNumber;
    @FXML private Label mainBalance;
    @FXML private Label mainSub;
    @FXML private Label dropType;
    @FXML private Label dropNumber;
    @FXML private Label dropBalance;
    @FXML private Label dropSub;
    @FXML private TableView<ActivityItem> activityTable;
    @FXML private TableColumn<ActivityItem, String> actionColumn;
    @FXML private TableColumn<ActivityItem, String> changeColumn;


    private double checkingBalance;
    private double savingsBalance;
    private int currentUserId;

    // Initializes the dashboard with user account data (called from MainController)
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

        loadActivity();
    }

    // Overloaded setData since we don't want to reaccess the view on scene change
    public void setData(int userId, double checking, double savings) {
        this.currentUserId    = userId;
        this.checkingBalance  = checking;
        this.savingsBalance   = savings;
        applyDataToView();
        loadActivity();
    }

    // Applies the current data to the view labels
    private void applyDataToView() {
        if (mainType == null || mainBalance == null || dropType == null || dropBalance == null) {
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

    // Adds money to checking and savings accounts
    public void addMoneyToAccounts(double chkAmount, double savAmount) {
        try {
            if (chkAmount < 0 || savAmount < 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Amount");
                alert.setHeaderText(null);
                alert.setContentText("Amounts must be non-negative");
                alert.showAndWait();
                return;
            }

            // Update the database
            UserRepository.depositToAccounts(currentUserId, chkAmount, savAmount);

            // Update local balances
            checkingBalance += chkAmount;
            savingsBalance  += savAmount;

            // Update the UI
            mainBalance.setText(String.format("$%.2f", checkingBalance));
            dropBalance.setText(String.format("$%.2f", savingsBalance));

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while depositing funds");
            alert.showAndWait();
        }
    }


    // Navigates to transfer view, passing current user data (userID + balances)
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

    // Handles user deletion with confirmation dialog (also deletes their accounts)
    // Then returns to main view
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

    // Navigates to loan management view, passing current user data
    @FXML
    private void onManageLoans(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/loan.fxml"));
        Parent userRoot = loader.load();
        LoanController controller = loader.getController();
        controller.setCurrentUserId(currentUserId);
        controller.setTotalBalance(checkingBalance + savingsBalance);
        controller.setAccountBalances(checkingBalance, savingsBalance);
        controller.loadLoanData();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(userRoot);
        System.out.println("button clicked");
    }

    // Withdraws money from checking and savings accounts (with validation)
    public void withdrawMoneyFromAccounts(double chkAmount, double savAmount) {
        try {
            if (chkAmount < 0 || savAmount < 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Amount");
                alert.setHeaderText(null);
                alert.setContentText("Amounts must be non-negative.");
                alert.showAndWait();
                return;
            }

            if (chkAmount > checkingBalance || savAmount > savingsBalance) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Insufficient Funds");
                alert.setHeaderText(null);
                alert.setContentText("You cannot withdraw more than the available balance.");
                alert.showAndWait();
                return;
            }

            // Update the database
            UserRepository.withdrawFromAccounts(currentUserId, chkAmount, savAmount);

            // Update local balances
            checkingBalance -= chkAmount;
            savingsBalance  -= savAmount;

            // Update the UI
            mainBalance.setText(String.format("$%.2f", checkingBalance));
            dropBalance.setText(String.format("$%.2f", savingsBalance));
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while withdrawing funds.");
            alert.showAndWait();
        }
    }

    // Adds interest to the savings account based on the current interest rate
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
            
            // Update the database and local balance
            UserRepository.depositToAccounts(currentUserId, 0, interestAmount);
            savingsBalance += interestAmount;
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


    // Opens a dialog to add or withdraw funds from accounts
    @FXML
    private void openFundsDialog(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Funds");
        DialogPane dialogPane = dialog.getDialogPane();

        // Configure dialog buttons: close, add, withdraw
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.LEFT);
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.APPLY);
        ButtonType withdrawButtonType = new ButtonType("Withdraw", ButtonBar.ButtonData.RIGHT);
        dialogPane.getButtonTypes().addAll(closeButtonType, addButtonType, withdrawButtonType);
        
        // Input fields for checking and savings amounts
        TextField checkingField = new TextField();
        checkingField.setPromptText("0.00");
        TextField savingsField = new TextField();
        savingsField.setPromptText("0.00");

        // layout for dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Amount to/from Checking: $"), checkingField);
        grid.addRow(1, new Label("Amount to/from Savings: $"), savingsField);

        dialogPane.setContent(grid);

        // Handle button actions
        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == closeButtonType) {
                return;
            }

            String chkText = checkingField.getText().trim();
            String savText = savingsField.getText().trim();

            double chkAmount = 0;
            double savAmount = 0;

            try {
                if (!chkText.isEmpty()) chkAmount = Double.parseDouble(chkText);
                if (!savText.isEmpty()) savAmount = Double.parseDouble(savText);
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please enter valid numeric amounts.");
                alert.showAndWait();
                return;
            }

            if (chkAmount == 0 && savAmount == 0) {
                return;
            }

            if (buttonType == addButtonType) {
                addMoneyToAccounts(chkAmount, savAmount);
            } else if (buttonType == withdrawButtonType) {
                withdrawMoneyFromAccounts(chkAmount, savAmount);
            }
        });
    }

    // Static inner class to help with the dialog pane
    public static class ActivityItem {
        private final SimpleStringProperty action;
        private final SimpleStringProperty change;

        public ActivityItem(String action, String change) {
            this.action = new SimpleStringProperty(action);
            this.change = new SimpleStringProperty(change);
        }

        public String getAction() {
            return action.get();
        }

        public String getChange() {
            return change.get();
        }
    }

    // Loads recent transfer activity for the current user into tableview
    private void loadActivity() {
        if (activityTable == null || actionColumn == null || changeColumn == null) {
            return;
        }
        actionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAction()));
        changeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getChange()));
        actionColumn.setReorderable(false);
        changeColumn.setReorderable(false);
        actionColumn.setResizable(false);
        changeColumn.setResizable(false);

        ObservableList<ActivityItem> items = FXCollections.observableArrayList();

        try {
            var records = UserRepository.getUserActivity(currentUserId);
            for (UserRepository.ActivityRecord r : records) {
                String changeStr = String.format("%+.2f", r.change); // e.g. +100.00 or -50.00
                items.add(new ActivityItem(r.action, changeStr));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        activityTable.setItems(items);
    }



    @FXML
    private void onBack(javafx.scene.input.MouseEvent e) throws java.io.IOException {
        Parent main = FXMLLoader.load(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
        ((Node)e.getSource()).getScene().setRoot(main);
    }

}
