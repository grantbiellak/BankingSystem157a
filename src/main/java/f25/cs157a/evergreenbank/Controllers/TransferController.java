package f25.cs157a.evergreenbank.Controllers;

import f25.cs157a.evergreenbank.Databases.UserRepository;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class TransferController {

    @FXML private ComboBox<String> toAccountTypeCombo;
    @FXML private TextField toUserIdField;
    @FXML private TextField toAmountField;
    @FXML private Pane arrow;
    @FXML private HBox dropdownCard;
    @FXML private Label mainType;
    @FXML private Label mainNumber;
    @FXML private Label mainBalance;
    @FXML private Label mainSub;
    @FXML private Label dropType;
    @FXML private Label dropNumber;
    @FXML private Label dropBalance;
    @FXML private Label dropSub;
    @FXML private Label errorLabel;

    private int currentUserId;
    private double savingsBalance;
    private double checkingBalance;
    private boolean expanded = false;

    public void setCurrentUserId(int id) {
        this.currentUserId = id;
    }

    public void setSavingsBalance(double balance) {
        this.savingsBalance = balance;
    }

    public void setCheckingBalance(double balance) {
        this.checkingBalance = balance;
    }

    public void loadAccountData() {
        mainBalance.setText(String.format("$%.2f", checkingBalance));
        dropBalance.setText(String.format("$%.2f", savingsBalance));

        mainType.setText("Checking");
        mainSub.setText("Available Balance");

        dropType.setText("Savings");
        dropSub.setText("Available Balance");

        String idText = "User " + currentUserId;
        mainNumber.setText(idText);
        dropNumber.setText(idText);

        if (toAccountTypeCombo != null && toAccountTypeCombo.getItems().contains("Checking")) {
            toAccountTypeCombo.setValue("Checking");
        }
    }

    @FXML
    private void handleTransfer() {
        try {
            errorLabel.setVisible(false);

            String fromChoice = mainType.getText();
            if (fromChoice == null || fromChoice.isBlank()) {
                throw new IllegalArgumentException("From account type is not set.");
            }

            if (toAccountTypeCombo == null || toAccountTypeCombo.getValue() == null) {
                throw new IllegalArgumentException("Please select a destination account type.");
            }
            String toChoice = toAccountTypeCombo.getValue();

            if (toAmountField == null || toAmountField.getText().isBlank()) {
                throw new IllegalArgumentException("Please enter an amount.");
            }
            double amount = Double.parseDouble(toAmountField.getText().trim());

            if (toUserIdField == null || toUserIdField.getText().isBlank()) {
                throw new IllegalArgumentException("Please enter a target user ID.");
            }
            int targetUserId = Integer.parseInt(toUserIdField.getText().trim());

            boolean success = UserRepository.transferByUserAndType(
                    currentUserId,
                    fromChoice.toUpperCase(),
                    targetUserId,
                    toChoice.toUpperCase(),
                    amount
            );

            if (success) {
                System.out.println("Transfer completed successfully!");

                // Update balances
                if (fromChoice.equalsIgnoreCase("CHECKING")) {
                    checkingBalance -= amount;
                } else if (fromChoice.equalsIgnoreCase("SAVINGS")) {
                    savingsBalance -= amount;
                }

                if (targetUserId == currentUserId) {
                    if (toChoice.equalsIgnoreCase("CHECKING")) {
                        checkingBalance += amount;
                    } else if (toChoice.equalsIgnoreCase("SAVINGS")) {
                        savingsBalance += amount;
                    }
                }

                refreshAccountCards();
                toAmountField.clear();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Transfer Successful");
                alert.setHeaderText(null);
                alert.setContentText(
                        "You have successfully transferred $" + String.format("%.2f", amount) +
                                " to User " + targetUserId + "."
                );
                alert.showAndWait();
            }

            else {
                System.out.println("Transfer failed.");
                errorLabel.setText("Transfer failed");
                errorLabel.setVisible(true);
            }

        } catch (NumberFormatException e) {
            System.err.println("Invalid number in amount or user id field: " + e.getMessage());
            errorLabel.setText("Invalid character or user id field");
            errorLabel.setVisible(true);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        } catch (SQLException e) {
            System.err.println("Database error during transfer: " + e.getMessage());
            try {
                String toChoice = toAccountTypeCombo.getValue();
                String fromChoice = mainType.getText();
                if (currentUserId == Integer.parseInt(toUserIdField.getText().trim())
                && toChoice.equalsIgnoreCase(fromChoice)) {
                    errorLabel.setText("Cannot transfer to the same account");
                }
                else if (savingsBalance >= Double.parseDouble(toAmountField.getText().trim())) {
                    errorLabel.setText("Transferring to a non-existing account");
                }
                else {
                    errorLabel.setText("Insufficient funds");
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Database error");
            }
            errorLabel.setVisible(true);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            errorLabel.setText("Unexpected error during transfer");
            errorLabel.setVisible(true);
        }
    }

    private void refreshAccountCards() {
        applyBalanceForType(mainType, mainBalance);
        applyBalanceForType(dropType, dropBalance);
    }

    private void applyBalanceForType(Label typeLabel, Label balanceLabel) {
        if (typeLabel == null || balanceLabel == null || typeLabel.getText() == null) return;

        String typeText = typeLabel.getText().trim().toUpperCase();
        switch (typeText) {
            case "CHECKING":
                balanceLabel.setText(String.format("$%.2f", checkingBalance));
                break;
            case "SAVINGS":
                balanceLabel.setText(String.format("$%.2f", savingsBalance));
                break;
            default:
                break;
        }
    }

    @FXML
    private void toggleArrow() {
        expanded = !expanded;

        dropdownCard.setVisible(expanded);
        dropdownCard.setManaged(expanded);
        double to = expanded ? -180 : -90;

        RotateTransition rt = new RotateTransition(Duration.millis(200), arrow);
        rt.setToAngle(to);
        rt.play();
    }

    @FXML
    private void onDropdownAccountClicked() {
        swap(mainType, dropType);
        swap(mainNumber, dropNumber);
        swap(mainBalance, dropBalance);
        swap(mainSub, dropSub);
        toggleArrow();
    }

    private void swap(Label a, Label b) {
        String temp = a.getText();
        a.setText(b.getText());
        b.setText(temp);
    }


    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/dashboard.fxml"));
        Parent mainRoot = loader.load();

        DashboardController controller = loader.getController();
        controller.setData(currentUserId, checkingBalance, savingsBalance);

        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainRoot);
    }

    @FXML
    private void onBackToMain(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
        Parent mainRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainRoot);
    }
}
