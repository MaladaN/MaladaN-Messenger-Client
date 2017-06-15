package net.strangled.maladan;


import net.MaladaN.Tor.thoughtcrime.ServerResponsePreKeyBundle;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.strangled.maladan.serializables.*;
import org.whispersystems.libsignal.SignalProtocolAddress;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Vector;

public class IncomingMessageThread implements Runnable {

    static boolean running = true;
    private static boolean registrationFlag;
    private static String password = "";
    private static String username = "";
    private static String loginResults = "";
    private static RegistrationResults registrationResults = null;

    //Soon to be implemented. (for actual user messages)
    private static Vector<Object> incomingMessages = new Vector<>();
    private Thread t;
    private InputStream stream;

    IncomingMessageThread(InputStream stream) {
        this.stream = stream;
    }

    static void setData(String password, String username) {
        IncomingMessageThread.password = password;
        IncomingMessageThread.username = username;
        IncomingMessageThread.registrationFlag = true;
    }

    static String getLoginResults() {
        return loginResults;
    }

    static void setLoginResults() {
        IncomingMessageThread.loginResults = "";
    }

    static RegistrationResults getRegistrationResults() {
        return registrationResults;
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
                    loginResults = "Failed to Login.";
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
        registrationFlag = false;
        while (password.equals("")) {
            Thread.sleep(1000);
        }
        SignalProtocolAddress serverAddress = new SignalProtocolAddress("SERVER", 0);
        byte[] encryptedHashedPassword = SignalCrypto.encryptByteMessage(Main.hashData(password), serverAddress, bundle.getPreKeyBundle());
        SignalEncryptedPasswordSend passwordSend = new SignalEncryptedPasswordSend(encryptedHashedPassword, DatatypeConverter.printBase64Binary(Main.hashData(username)));

        OutgoingMessageThread.addNewMessage(passwordSend);
        password = "";
        username = "";
    }

    private void returnLoginResults(EncryptedLoginState encryptedLoginState) throws Exception {
        byte[] serializedLoginResponseState = SignalCrypto.decryptMessage(encryptedLoginState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        LoginResponseState state = (LoginResponseState) Main.reconstructSerializedObject(serializedLoginResponseState);

        if (state.isValidLogin()) {
            loginResults = "Logged In Successfully";

        } else {
            loginResults = "Failed to Login.";
        }
    }

    private void returnRegistrationResults(EncryptedRegistrationState encryptedRegistrationState) throws Exception {
        byte[] serializedRegistrationResponseState = SignalCrypto.decryptMessage(encryptedRegistrationState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        RegistrationResponseState state = (RegistrationResponseState) Main.reconstructSerializedObject(serializedRegistrationResponseState);
        boolean loginState = state.isValidRegistration();
        if (loginState) {
            registrationResults = new RegistrationResults("Successfully Registered.", true);
        } else {
            registrationResults = new RegistrationResults("Registration Failed.", false);
        }
    }

    void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

}
