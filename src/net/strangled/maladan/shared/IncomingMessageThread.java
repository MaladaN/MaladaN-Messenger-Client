package net.strangled.maladan.shared;


import net.MaladaN.Tor.thoughtcrime.ServerResponsePreKeyBundle;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.strangled.maladan.cli.AuthResults;
import net.strangled.maladan.cli.Main;
import net.strangled.maladan.serializables.Authentication.*;
import net.strangled.maladan.serializables.Messaging.EncryptedMMessageObject;
import net.strangled.maladan.serializables.Messaging.MMessageObject;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyBundle;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Vector;

public class IncomingMessageThread implements Runnable {

    private static String password;
    private static String username;
    private static boolean registrationFlag;
    private static AuthResults loginResults = null;
    private static PreKeyBundle userBundle = null;
    private static Vector<MMessageObject> incomingMessages = new Vector<>();
    public boolean running = true;
    private Thread t;
    private InputStream stream;


    public IncomingMessageThread(InputStream stream) {
        this.stream = stream;
    }

    public static synchronized void setCredentials(String password, String username) {
        IncomingMessageThread.password = password;
        IncomingMessageThread.username = username;
        registrationFlag = true;
    }

    public static synchronized String getPassword() {
        return IncomingMessageThread.password;
    }

    public static synchronized String getUsername() {
        return IncomingMessageThread.username;
    }

    public static synchronized void clearLoginData() {
        IncomingMessageThread.password = "";
        IncomingMessageThread.username = "";
    }

    public static synchronized AuthResults getAuthResults() {
        return IncomingMessageThread.loginResults;
    }

    public static synchronized void setAuthResults(AuthResults results) {
        IncomingMessageThread.loginResults = results;
    }

    public static synchronized void clearAuthResults() {
        IncomingMessageThread.loginResults = null;
    }

    public static synchronized PreKeyBundle getUserBundle() {
        return IncomingMessageThread.userBundle;
    }

    public static synchronized void setUserBundle(PreKeyBundle userBundle) {
        IncomingMessageThread.userBundle = userBundle;
    }

    public static synchronized List<MMessageObject> getIncomingMessages() {
        Vector<MMessageObject> objects = new Vector<>();
        objects.addAll(IncomingMessageThread.incomingMessages);
        return objects;
    }

    public static synchronized void deleteMessageObjects(List<MMessageObject> objectsToRemove) {
        IncomingMessageThread.incomingMessages.removeAll(objectsToRemove);
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

                } else if (incoming instanceof EncryptedLoginResponseState) {
                    EncryptedLoginResponseState encryptedLoginResponseState = (EncryptedLoginResponseState) incoming;
                    returnLoginResults(encryptedLoginResponseState);

                } else if (incoming instanceof LoginResponseState) {
                    IncomingMessageThread.setAuthResults(new AuthResults("Failed to Login", false));

                } else if (incoming instanceof EncryptedRegistrationResponseState) {
                    EncryptedRegistrationResponseState encryptedRegistrationResponseState = (EncryptedRegistrationResponseState) incoming;
                    returnRegistrationResults(encryptedRegistrationResponseState);

                } else if (incoming instanceof EncryptedClientPreKeyBundle) {
                    EncryptedClientPreKeyBundle encryptedClientPreKeyBundle = (EncryptedClientPreKeyBundle) incoming;
                    handleRequestedUserPreKeyBundle(encryptedClientPreKeyBundle);

                } else if (incoming instanceof EncryptedMMessageObject) {
                    EncryptedMMessageObject incomingMessage = (EncryptedMMessageObject) incoming;
                    handleIncomingMMessage(incomingMessage);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registrationSendPassword(ServerResponsePreKeyBundle bundle) throws Exception {
        while (IncomingMessageThread.getPassword().equals("")) {
            Thread.sleep(1000);
        }

        String password = IncomingMessageThread.getPassword();
        String username = IncomingMessageThread.getUsername();

        SignalProtocolAddress serverAddress = new SignalProtocolAddress("SERVER", 0);

        byte[] hashedPassword = Main.hashData(password);
        byte[] encryptedHashedPassword = SignalCrypto.encryptByteMessage(hashedPassword, serverAddress, bundle.getPreKeyBundle());

        byte[] hashedUsername = Main.hashData(username);
        String base64Username = DatatypeConverter.printBase64Binary(hashedUsername);

        SignalEncryptedPasswordSend passwordSend = new SignalEncryptedPasswordSend(encryptedHashedPassword, base64Username);

        OutgoingMessageThread.addOutgoingMessage(passwordSend);

        IncomingMessageThread.clearLoginData();
        registrationFlag = false;
    }

    private void returnLoginResults(EncryptedLoginResponseState encryptedLoginResponseState) throws Exception {
        byte[] serializedLoginResponseState = SignalCrypto.decryptMessage(encryptedLoginResponseState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        LoginResponseState state = (LoginResponseState) net.strangled.maladan.cli.Main.reconstructSerializedObject(serializedLoginResponseState);

        if (state.isValidLogin()) {
            IncomingMessageThread.setAuthResults(new AuthResults("Logged In Successfully", true));
        } else {
            IncomingMessageThread.setAuthResults(new AuthResults("Failed to Login.", false));
        }
    }

    private void returnRegistrationResults(EncryptedRegistrationResponseState encryptedRegistrationResponseState) throws Exception {
        byte[] serializedRegistrationResponseState = SignalCrypto.decryptMessage(encryptedRegistrationResponseState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        RegistrationResponseState state = (RegistrationResponseState) net.strangled.maladan.cli.Main.reconstructSerializedObject(serializedRegistrationResponseState);
        boolean loginState = state.isValidRegistration();

        if (loginState) {
            IncomingMessageThread.setAuthResults(new AuthResults("Successfully Registered.", true));
        } else {
            IncomingMessageThread.setAuthResults(new AuthResults("Registration Failed.", false));
        }
    }

    private void handleRequestedUserPreKeyBundle(EncryptedClientPreKeyBundle bundle) throws Exception {
        byte[] serializedResponseBundle = SignalCrypto.decryptMessage(bundle.getEncryptedSerializedClientPreKeyBundle(), new SignalProtocolAddress("SERVER", 0));
        ServerResponsePreKeyBundle serverResponsePreKeyBundle = (ServerResponsePreKeyBundle) Main.reconstructSerializedObject(serializedResponseBundle);

        IncomingMessageThread.setUserBundle(serverResponsePreKeyBundle.getPreKeyBundle());
    }

    private void handleIncomingMMessage(EncryptedMMessageObject object) throws Exception {
        byte[] serializedMMessageObject = SignalCrypto.decryptMessage(object.getEncryptedSerializedMMessageObject(), new SignalProtocolAddress("SERVER", 0));
        MMessageObject messageObject = (MMessageObject) Main.reconstructSerializedObject(serializedMMessageObject);

        incomingMessages.add(messageObject);
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

}
