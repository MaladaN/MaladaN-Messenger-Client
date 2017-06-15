package net.strangled.maladan;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.strangled.maladan.serializables.ServerInit;
import net.strangled.maladan.serializables.ServerLogin;
import org.whispersystems.libsignal.SignalProtocolAddress;

import javax.xml.bind.DatatypeConverter;


public class SignInController {
    //Shared Between Registration and Login.
    @FXML
    private Button registerButton;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;

    //Login Fields
    @FXML
    private TextField usernameLogin;
    @FXML
    private PasswordField passwordLogin;
    @FXML
    private Button submitSignIn;

    //Register Fields
    @FXML
    private PasswordField passwordVerify;
    @FXML
    private TextField uniqueValidationKey;
    @FXML
    private Button submitRegistration;

    private boolean windowStatus;

    @FXML
    private void signIn_RegistrationSwitcher(ActionEvent event) {
        Stage stage = null;
        Parent root = null;

        try {
            if (event.getSource().equals(registerButton)) {
                stage = (Stage) registerButton.getScene().getWindow();

                if (!LocalLoginDataStore.dataSaved()) {
                    root = FXMLLoader.load(getClass().getResource("Register.fxml"));

                } else {
                    registerButton.setDisable(true);
                    windowStatus = true;
                }

            } else if (event.getSource().equals(loginButton)) {

                if (!windowStatus) {
                    stage = (Stage) loginButton.getScene().getWindow();
                    root = FXMLLoader.load(getClass().getResource("SignIn.fxml"));

                } else {
                    registerButton.setDisable(true);
                }
            }
            if (stage != null && root != null) {
                Scene scene = new Scene(root, 375, 500);
                stage.setResizable(false);
                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //need to change this a little bit.
    //needs to only accept the user registered once with.
    //also need to make it so you can only register once (You only have one identity key).
    @FXML
    private void signIn() {
        String username = usernameLogin.getText();
        String password = passwordLogin.getText();
        if (!username.isEmpty() && !password.isEmpty()) {
            errorLabel.setText("");

            try {
                Thread loginWork = new Thread(() -> {

                    try {
                        SignalProtocolAddress address = new SignalProtocolAddress("SERVER", 0);
                        ServerLogin login = new ServerLogin(DatatypeConverter.printBase64Binary(Main.hashData(username)), SignalCrypto.encryptByteMessage(Main.hashData(password), address, null));
                        OutgoingMessageThread.addNewMessage(login);

                        while (IncomingMessageThread.getLoginResults().equals("")) {
                            Thread.sleep(1000);
                        }

                        String data = IncomingMessageThread.getLoginResults();
                        IncomingMessageThread.setLoginResults();

                        Platform.runLater(() -> {
                            errorLabel.setText(data);
                            try {
                                switchToMainInterface();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                loginWork.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid Data Entered");
        }

    }

    @FXML
    private void submitRegistration() {
        String username = usernameLogin.getText();
        String password = passwordLogin.getText();
        String verifyPassword = passwordVerify.getText();
        String uniqueId = uniqueValidationKey.getText();

        if (password.equals(verifyPassword) && !username.isEmpty() && !password.isEmpty() && !verifyPassword.isEmpty() && !uniqueId.isEmpty()) {
            errorLabel.setText("");

            try {
                Thread registrationWork = new Thread(() -> {

                    try {
                        ServerInit init = new ServerInit(Main.hashData(username), uniqueId, Main.getData());
                        OutgoingMessageThread.addNewMessage(init);
                        IncomingMessageThread.setData(password, username);

                        while (IncomingMessageThread.getRegistrationResults() == null) {
                            Thread.sleep(1000);
                        }

                        String data = IncomingMessageThread.getRegistrationResults().getFormattedResults();
                        boolean valid = IncomingMessageThread.getRegistrationResults().isValid();

                        Platform.runLater(() -> {
                            errorLabel.setText(data);

                            if (valid) {
                                disableRegistration();
                                try {
                                    LocalLoginDataStore.saveLocaluser(new User(true));
                                    switchToMainInterface();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                registrationWork.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid Data Entered");
        }

    }

    private void disableRegistration() {
        this.registerButton.setDisable(true);
        this.submitRegistration.setDisable(true);
        this.uniqueValidationKey.setDisable(true);
    }

    private void switchToMainInterface() throws Exception {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
        Parent root = FXMLLoader.load(getClass().getResource("MainInterface.fxml"));
        Scene scene = new Scene(root, 900, 600);
        stage.setResizable(true);

        stage.setScene(scene);
        stage.show();

    }

}