package net.strangled.maladan.shared;


import net.MaladaN.Tor.thoughtcrime.ServerResponsePreKeyBundle;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.strangled.maladan.cli.AuthResults;
import net.strangled.maladan.cli.Main;
import net.strangled.maladan.serializables.Authentication.*;
import net.strangled.maladan.serializables.Messaging.EncryptedMMessageObject;
import net.strangled.maladan.serializables.Messaging.MMessageObject;
import org.whispersystems.libsignal.SignalProtocolAddress;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class IncomingMessageThread implements Runnable {

    public boolean running = true;
    private Thread t;
    private InputStream stream;

    public IncomingMessageThread(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(stream);

            while (running) {
                Object incoming = in.readObject();

                if (incoming instanceof ServerResponsePreKeyBundle && StaticComms.isRegistrationFlag()) {
                    ServerResponsePreKeyBundle serverResponsePreKeyBundle = (ServerResponsePreKeyBundle) incoming;
                    registrationSendPassword(serverResponsePreKeyBundle);

                } else if (incoming instanceof EncryptedLoginResponseState) {
                    EncryptedLoginResponseState encryptedLoginResponseState = (EncryptedLoginResponseState) incoming;
                    returnLoginResults(encryptedLoginResponseState);

                } else if (incoming instanceof LoginResponseState) {
                    StaticComms.setAuthResults(new AuthResults("Failed to Login", false));

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
        while (StaticComms.getPassword().equals("")) {
            Thread.sleep(1000);
        }

        String password = StaticComms.getPassword();
        String username = StaticComms.getUsername();

        SignalProtocolAddress serverAddress = new SignalProtocolAddress("SERVER", 0);

        byte[] hashedPassword = net.strangled.maladan.cli.Main.hashData(password);
        byte[] encryptedHashedPassword = SignalCrypto.encryptByteMessage(hashedPassword, serverAddress, bundle.getPreKeyBundle());

        byte[] hashedUsername = net.strangled.maladan.cli.Main.hashData(username);
        String base64Username = DatatypeConverter.printBase64Binary(hashedUsername);

        SignalEncryptedPasswordSend passwordSend = new SignalEncryptedPasswordSend(encryptedHashedPassword, base64Username);

        StaticComms.addOutgoingMessage(passwordSend);

        StaticComms.clearLoginData();
        StaticComms.falsifyRegistrationFlag();
    }

    private void returnLoginResults(EncryptedLoginResponseState encryptedLoginResponseState) throws Exception {
        byte[] serializedLoginResponseState = SignalCrypto.decryptMessage(encryptedLoginResponseState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        LoginResponseState state = (LoginResponseState) net.strangled.maladan.cli.Main.reconstructSerializedObject(serializedLoginResponseState);

        if (state.isValidLogin()) {
            StaticComms.setAuthResults(new AuthResults("Logged In Successfully", true));
        } else {
            StaticComms.setAuthResults(new AuthResults("Failed to Login.", false));
        }
    }

    private void returnRegistrationResults(EncryptedRegistrationResponseState encryptedRegistrationResponseState) throws Exception {
        byte[] serializedRegistrationResponseState = SignalCrypto.decryptMessage(encryptedRegistrationResponseState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        RegistrationResponseState state = (RegistrationResponseState) net.strangled.maladan.cli.Main.reconstructSerializedObject(serializedRegistrationResponseState);
        boolean loginState = state.isValidRegistration();

        if (loginState) {
            StaticComms.setAuthResults(new AuthResults("Successfully Registered.", true));
        } else {
            StaticComms.setAuthResults(new AuthResults("Registration Failed.", false));
        }
    }

    private void handleRequestedUserPreKeyBundle(EncryptedClientPreKeyBundle bundle) throws Exception {
        byte[] serializedResponseBundle = SignalCrypto.decryptMessage(bundle.getEncryptedSerializedClientPreKeyBundle(), new SignalProtocolAddress("SERVER", 0));
        ServerResponsePreKeyBundle serverResponsePreKeyBundle = (ServerResponsePreKeyBundle) Main.reconstructSerializedObject(serializedResponseBundle);

        StaticComms.setUserBundle(serverResponsePreKeyBundle.getPreKeyBundle());
    }

    private void handleIncomingMMessage(EncryptedMMessageObject object) throws Exception {
        byte[] serializedMMessageObject = SignalCrypto.decryptMessage(object.getEncryptedSerializedMMessageObject(), new SignalProtocolAddress("SERVER", 0));
        MMessageObject messageObject = (MMessageObject) Main.reconstructSerializedObject(serializedMMessageObject);

        StaticComms.addIncomingMessage(messageObject);
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

}
