package f25.cs157a.evergreenbank.Controllers;

import f25.cs157a.evergreenbank.Databases.UserRepository;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class TransferController {

    @FXML private TextField toAccountField;
    @FXML private TextField amountField;
    @FXML private ToggleGroup fromToggleGroup;
    @FXML private ToggleGroup toToggleGroup;
    @FXML private RadioButton fromAccountRadioButtonSavings;
    @FXML private RadioButton fromAccountRadioButtonChecking;
    @FXML private RadioButton toAccountRadioButtonSavings;
    @FXML private RadioButton toAccountRadioButtonChecking;
    @FXML private Pane arrow;

    private int currentUserId;
    private boolean expanded = false;
    // This is to set userId to be something else, we need it to be able to tell who the current user is
    public void setCurrentUserId(int id) {
        this.currentUserId = id;
    }


    // TODO make it lowkey fancy af
    // Currently this just handles transfers
    // The options are to transfer from x to y, and you can specify which account you want to transfer into
    @FXML
    private void handleTransfer() {
        System.out.println(currentUserId);
        RadioButton fromSelected = (RadioButton) fromToggleGroup.getSelectedToggle();
        RadioButton toSelected = (RadioButton) toToggleGroup.getSelectedToggle();

        String fromChoice = null;
        String toChoice = null;
        if (fromSelected != null && toSelected != null) {
            fromChoice = fromSelected.getText().toUpperCase();
            toChoice = toSelected.getText().toUpperCase();

        }

        try {
            double amount = Double.parseDouble(amountField.getText());
            int targetUserId = Integer.parseInt(toAccountField.getText());
            boolean success = UserRepository.transferByUserAndType(
                    currentUserId, fromChoice,
                    targetUserId, toChoice,
                    amount
            );

            if (success) {
                System.out.println("Transfer completed successfully!");
            }
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
        RotateTransition rt = new RotateTransition(Duration.millis(200), arrow);
        rt.setFromAngle(expanded ? 180 : 0);
        rt.setToAngle(expanded ? 0 : 180);
        expanded = !expanded;
        rt.play();
    }

    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/dashboard.fxml"));
        Parent mainRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainRoot);
    }

    // TOP BAR METHOD TO BE CHANGED/REFACTORED
    @FXML
    private void onBackToMain(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
        Parent mainRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainRoot);
    }
}
