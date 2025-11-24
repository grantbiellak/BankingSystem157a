package f25.cs157a.evergreenbank;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

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

    private int currentUserId;


    public void setCurrentUserId(int id) {
        this.currentUserId = id;
    }


    // TODO make it lowkey fancy af
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
    private void onBackToMain(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent mainRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainRoot);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
    }
}
