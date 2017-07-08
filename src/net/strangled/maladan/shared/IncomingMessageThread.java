package net.strangled.maladan.shared;


import net.MaladaN.Tor.thoughtcrime.ServerResponsePreKeyBundle;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.strangled.maladan.serializables.*;
import org.whispersystems.libsignal.SignalProtocolAddress;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Vector;

public class IncomingMessageThread implements Runnable {

    public static boolean running = true;
    private static boolean registrationFlag;
    private static String password = "";
    private static String username = "";
    private static AuthResults loginResults = null;

    //Soon to be implemented. (for actual user messages)
    private static Vector<Object> incomingMessages = new Vector<>();
    private Thread t;
    private InputStream stream;

    public IncomingMessageThread(InputStream stream) {
        this.stream = stream;
    }

    public static void setData(String password, String username) {
        IncomingMessageThread.password = password;
        IncomingMessageThread.username = username;
        IncomingMessageThread.registrationFlag = true;
    }

    public static AuthResults getAuthResults() {
        return loginResults;
    }

    public static void setAuthResults() {
        IncomingMessageThread.loginResults = null;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(stream);

            while (running) {
                Object incoming = in.readObject();

                if (incoming instanceof ServerResponsePreKeyBundle && registrationFlag) {
                    ServerResponsePreKeyBundle serverResponsePreKeyBundle = (ServerResponsePreKeyBundle) incoming;
                    registrationSendPassword(serverResponsePreKeyBundle);

                } else if (incoming instanceof EncryptedLoginState) {
                    EncryptedLoginState encryptedLoginState = (EncryptedLoginState) incoming;
                    returnLoginResults(encryptedLoginState);

                } else if (incoming instanceof LoginResponseState) {
                    loginResults = new AuthResults("Failed to Login", false);

                } else if (incoming instanceof EncryptedRegistrationState) {
                    EncryptedRegistrationState encryptedRegistrationState = (EncryptedRegistrationState) incoming;
                    returnRegistrationResults(encryptedRegistrationState);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registrationSendPassword(ServerResponsePreKeyBundle bundle) throws Exception {
        while (password.equals("")) {
            Thread.sleep(1000);
        }

        SignalProtocolAddress serverAddress = new SignalProtocolAddress("SERVER", 0);

        byte[] hashedPassword = net.strangled.maladan.cli.Main.hashData(password);
        byte[] encryptedHashedPassword = SignalCrypto.encryptByteMessage(hashedPassword, serverAddress, bundle.getPreKeyBundle());

        byte[] hashedUsername = net.strangled.maladan.cli.Main.hashData(username);
        String base64Username = DatatypeConverter.printBase64Binary(hashedUsername);

        SignalEncryptedPasswordSend passwordSend = new SignalEncryptedPasswordSend(encryptedHashedPassword, base64Username);

        OutgoingMessageThread.addNewMessage(passwordSend);

        password = "";
        username = "";
        registrationFlag = false;
    }

    private void returnLoginResults(EncryptedLoginState encryptedLoginState) throws Exception {
        byte[] serializedLoginResponseState = SignalCrypto.decryptMessage(encryptedLoginState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        LoginResponseState state = (LoginResponseState) net.strangled.maladan.cli.Main.reconstructSerializedObject(serializedLoginResponseState);

        if (state.isValidLogin()) {
            loginResults = new AuthResults("Logged In Successfully", true);
        } else {
            loginResults = new AuthResults("Failed to Login.", false);
        }
    }

    private void returnRegistrationResults(EncryptedRegistrationState encryptedRegistrationState) throws Exception {
        byte[] serializedRegistrationResponseState = SignalCrypto.decryptMessage(encryptedRegistrationState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        RegistrationResponseState state = (RegistrationResponseState) net.strangled.maladan.cli.Main.reconstructSerializedObject(serializedRegistrationResponseState);
        boolean loginState = state.isValidRegistration();

        if (loginState) {
            loginResults = new AuthResults("Successfully Registered.", true);
        } else {
            loginResults = new AuthResults("Registration Failed.", false);
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

}
