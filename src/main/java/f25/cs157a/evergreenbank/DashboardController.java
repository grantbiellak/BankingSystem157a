package f25.cs157a.evergreenbank;

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
    
    @FXML private Label welcomeName;
    @FXML private Label checkingBalanceLabel;
    @FXML private Label savingsBalanceLabel;
    @FXML private DialogPane transferDialog;

    private int currentUserId;

    // signincontrolelr calls this after login
    // jus display the balances and routing nums
    public void setData(String fullName, UserRepository.AccountsView view) {
        //jus the top label we a friendly bank :D
        currentUserId = view.userID;
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

    @FXML
    private void onTransfer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("transfer.fxml"));
        Parent userRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();

        TransferController ctrl = loader.getController();
        ctrl.setCurrentUserId(currentUserId);
        scene.setRoot(userRoot);
    }

    @FXML
    private void onBack(javafx.scene.input.MouseEvent e) throws java.io.IOException {
        Parent main = FXMLLoader.load(getClass().getResource("main-view.fxml"));
        ((Node)e.getSource()).getScene().setRoot(main);
    }
}
