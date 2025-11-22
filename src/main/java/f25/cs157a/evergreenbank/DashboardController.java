package f25.cs157a.evergreenbank;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class DashboardController {
    
    @FXML private Label welcomeName;
    @FXML private Label checkingRoutingLabel;
    @FXML private Label checkingBalanceLabel;
    @FXML private Label savingsRoutingLabel;
    @FXML private Label savingsBalanceLabel;
    @FXML
    private TextField toAccountField;
    @FXML
    private TextField amountField;

    private int currentUserId;       // <-- add these

    // signincontrolelr calls this after login
    // jus display the balances and routing nums
    public void setData(String fullName, UserRepository.AccountsView view) {
        //jus the top label we a friendly bank :D
        this.currentUserId = view.userID;       // <-- remember the logged-in user
        if (welcomeName != null) {
            welcomeName.setText("Welcome, " + fullName + "!");
        }
        if (checkingBalanceLabel != null) {
            checkingBalanceLabel.setText(String.format("Checking Balance: $%.2f", view.checkingBalance));
        }
        if (savingsBalanceLabel != null) {
            savingsBalanceLabel.setText(String.format("Savings Balance: $%.2f", view.savingsBalance));
        }
    }

    // TODO make it lowkey fancy af
    @FXML
    private void onTransfer() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            int targetUserId = Integer.parseInt(toAccountField.getText());
            boolean success = UserRepository.transferByUserAndType(
                    currentUserId, "SAVINGS",
                    targetUserId, "CHECKING",
                    amount
            );

            if (success) {
                System.out.println("Transfer completed successfully!");
                // Optional: show confirmation in UI
                // new Alert(Alert.AlertType.INFORMATION, "Transfer successful!").showAndWait();
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
            // Optional: new Alert(Alert.AlertType.WARNING, "Invalid input: " + e.getMessage()).showAndWait();
        } catch (SQLException e) {
            System.err.println("Database error during transfer: " + e.getMessage());
            // Optional: new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage()).showAndWait();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            // Optional: new Alert(Alert.AlertType.ERROR, "Unexpected error occurred.").showAndWait();
        }
    }


    @FXML
    private void onBack(javafx.scene.input.MouseEvent e) throws java.io.IOException {
        Parent main = FXMLLoader.load(getClass().getResource("main-view.fxml"));
        ((Node)e.getSource()).getScene().setRoot(main);
    }
}
