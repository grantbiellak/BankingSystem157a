package f25.cs157a.evergreenbank.Controllers;

import f25.cs157a.evergreenbank.Databases.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;


import java.io.IOException;
import java.sql.SQLException;

// Controlls the loan view where users can request and repay loans
public class LoanController {

    @FXML private Label totalBalanceLabel;
    @FXML private Label unpaidLoanLabel;
    @FXML private TextField loanAmountField;
    @FXML private TextField repaymentAmountField;
    @FXML private ChoiceBox<String> accountChoiceBox;

    private int currentUserId;
    private double totalBalance;
    private double unpaidLoanAmount;
    private double checkingBalance;
    private double savingsBalance;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
        totalBalanceLabel.setText(String.format("Total Balance: $%.2f", totalBalance));
    }

    public void setAccountBalances(double checkingBalance, double savingsBalance) {
        this.checkingBalance = checkingBalance;
        this.savingsBalance = savingsBalance;
    }

    // Loads unpaid loan amount from database to display
    public void loadLoanData() {
        try {
            unpaidLoanAmount = UserRepository.getUnpaidLoanAmount(currentUserId);
            unpaidLoanLabel.setText(String.format("Unpaid Loan: $%.2f", unpaidLoanAmount));
        } catch (SQLException e) {
            unpaidLoanLabel.setText("Unpaid Loan: Error fetching data");
            e.printStackTrace();
        }
    }


    // Handles loan request submission, if valid input then calls UserRepository to process
    @FXML
    private void submitLoanRequest(ActionEvent event) {
        String loanAmountText = loanAmountField.getText().trim();
        double loanAmount;

        double loanCap = 1000000.00;

        try {
            loanAmount = Double.parseDouble(loanAmountText);

            if (loanAmount <= 0) {
                showError("Invalid Amount", "Loan amount must be greater than zero.");
                return;
            }

            if (loanAmount > totalBalance * 2) {
                showError("Loan Request Denied", "Loan amount exceeds double your total balance.");
                return;
            }

            if (loanAmount > loanCap) {
                showError("Loan Request Denied", "Loan amount exceeds the maximum cap of $1,000,000.00.");
                return;
            }

            boolean hasUnpaidLoan = UserRepository.hasUnpaidLoan(currentUserId);
            if (hasUnpaidLoan) {
                showError("Loan Request Denied", "You already have an unpaid loan.");
                return;
            }

            boolean loanGranted = UserRepository.requestLoan(currentUserId, loanAmount);
            if (loanGranted) {
                UserRepository.depositToAccounts(currentUserId, loanAmount, 0); // Deposit loan into checking account
                checkingBalance += loanAmount;
                unpaidLoanAmount += loanAmount;
                loadLoanData();

                totalBalance = checkingBalance + savingsBalance;
                totalBalanceLabel.setText(String.format("Total Balance: $%.2f", totalBalance));

                showSuccess("Loan Approved", "Your loan request has been approved.");
                loanAmountField.clear();
            } else {
                showError("Loan Request Failed", "An error occurred while processing your loan request.");
            }

        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter a valid loan amount.");
        } catch (SQLException e) {
            showError("Database Error", "An error occurred while accessing the database.");
            e.printStackTrace();
        }
    }


    // Handles repaying part or all of the unpaid loan from checkings or savings
    @FXML
    private void submitLoanRepayment(ActionEvent event) {
        String repaymentAmountText = repaymentAmountField.getText().trim();
        double repaymentAmount;

        try {
            repaymentAmount = Double.parseDouble(repaymentAmountText);

            if (repaymentAmount <= 0) {
                showError("Invalid Amount", "Repayment amount must be greater than zero.");
                return;
            }

            String selectedAccount = accountChoiceBox.getValue();
            if (selectedAccount == null) {
                showError("Invalid Selection", "Please select an account to pay from.");
                return;
            }

            double accountBalance = selectedAccount.equals("Checkings") ? checkingBalance : savingsBalance;

            if (repaymentAmount > accountBalance) {
                showError("Insufficient Funds", "Not enough funds in the selected account.");
                return;
            }

            double amountToDeduct = Math.min(repaymentAmount, unpaidLoanAmount);
            boolean repaymentSuccess = UserRepository.payLoan(currentUserId, amountToDeduct, selectedAccount);

            if (repaymentSuccess) {
                if (selectedAccount.equals("Checkings")) {
                    checkingBalance -= amountToDeduct;
                } else {
                    savingsBalance -= amountToDeduct;
                }

                unpaidLoanAmount -= amountToDeduct;
                if (unpaidLoanAmount == 0) {
                    UserRepository.markLoanAsPaid(currentUserId);
                }

                totalBalance = checkingBalance + savingsBalance;
                totalBalanceLabel.setText(String.format("Total Balance: $%.2f", totalBalance));
                loadLoanData();
                showSuccess("Repayment Successful", String.format("You repaid $%.2f towards your loan.", amountToDeduct));
                repaymentAmountField.clear();
            } else {
                showError("Repayment Failed", "An error occurred while processing your repayment.");
            }

        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter a valid repayment amount.");
        } catch (SQLException e) {
            showError("Database Error", "An error occurred while accessing the database.");
            e.printStackTrace();
        }
    }


    // return to dashboard view, passing user data
    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/dashboard.fxml"));
        Parent mainRoot = loader.load();

        DashboardController controller = loader.getController();
        controller.setData(currentUserId, checkingBalance, savingsBalance);

        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainRoot);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onBackToMain(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/f25/cs157a/evergreenbank/main-view.fxml"));
        Parent mainRoot = loader.load();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainRoot);
    }
}