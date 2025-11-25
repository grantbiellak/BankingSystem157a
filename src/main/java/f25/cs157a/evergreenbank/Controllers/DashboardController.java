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

public class DashboardController {

    @FXML private Label checkingBalanceLabel;
    @FXML private Label savingsBalanceLabel;

    private int currentUserId;

    // Set the data from the view, this will display user info when we open the dashboard(after sign in)
    public void setData(UserRepository.AccountsView view) {
        currentUserId = view.userID; // This is the most important part of this method, allows us to show userID to other controllers
        if (checkingBalanceLabel != null) {
            checkingBalanceLabel.setText(String.format("Checking Balance: $%.2f", view.checkingBalance));
        }
        if (savingsBalanceLabel != null) {
            savingsBalanceLabel.setText(String.format("Savings Balance: $%.2f", view.savingsBalance));
        }
    }

    // When we click the transfer button we need to switch to the transfer scene, also set the userID in the other controller
    @FXML
    private void onTransfer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/transfer.fxml"));
        Parent userRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        TransferController ctrl = loader.getController();
        ctrl.setCurrentUserId(currentUserId);
        scene.setRoot(userRoot);
    }


    // This onBack sends us back to the main view, it is the top bar onBack that we have been using
    // THIS IS A TOP BAR METHOD (future grant)
    @FXML
    private void onBack(javafx.scene.input.MouseEvent e) throws java.io.IOException {
        Parent main = FXMLLoader.load(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
        ((Node)e.getSource()).getScene().setRoot(main);
    }
}
