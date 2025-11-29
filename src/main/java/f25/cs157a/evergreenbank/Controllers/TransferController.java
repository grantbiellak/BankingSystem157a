package f25.cs157a.evergreenbank.Controllers;

import f25.cs157a.evergreenbank.Databases.UserRepository;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    // populate the cards (call this after setting userId + balances)
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
            String fromChoice = mainType.getText();
            if (fromChoice == null || fromChoice.isBlank()) {
                throw new IllegalArgumentException("From account type is not set.");
            }

            if (toAccountTypeCombo == null || toAccountTypeCombo.getValue() == null) {
                throw new IllegalArgumentException("Please select a destination account type.");
            }
            String toChoice = toAccountTypeCombo.getValue();

            // amount
            if (toAmountField == null || toAmountField.getText().isBlank()) {
                throw new IllegalArgumentException("Please enter an amount.");
            }
            double amount = Double.parseDouble(toAmountField.getText().trim());

            // target user id
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
            } else {
                System.out.println("Transfer failed.");
            }

        } catch (NumberFormatException e) {
            System.err.println("Invalid number in amount or user id field: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error during transfer: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
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
