package net.strangled.maladan.shared;


import net.MaladaN.Tor.thoughtcrime.MMessageObject;
import net.MaladaN.Tor.thoughtcrime.ServerResponsePreKeyBundle;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.strangled.maladan.serializables.*;
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

                } else if (incoming instanceof EncryptedLoginState) {
                    EncryptedLoginState encryptedLoginState = (EncryptedLoginState) incoming;
                    returnLoginResults(encryptedLoginState);

                } else if (incoming instanceof LoginResponseState) {
                    StaticComms.setAuthResults(new AuthResults("Failed to Login", false));

                } else if (incoming instanceof EncryptedRegistrationState) {
                    EncryptedRegistrationState encryptedRegistrationState = (EncryptedRegistrationState) incoming;
                    returnRegistrationResults(encryptedRegistrationState);

                } else if (incoming instanceof ServerResponsePreKeyBundle && !StaticComms.isRegistrationFlag()) {
                    ServerResponsePreKeyBundle serverResponsePreKeyBundle = (ServerResponsePreKeyBundle) incoming;
                    handleRequestedUserPreKeyBundle(serverResponsePreKeyBundle);

                } else if (incoming instanceof MMessageObject) {
                    MMessageObject incomingMessage = (MMessageObject) incoming;
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

        OutgoingMessageThread.addNewMessage(passwordSend);

        StaticComms.clearLoginData();
        StaticComms.falsifyRegistrationFlag();
    }

    private void returnLoginResults(EncryptedLoginState encryptedLoginState) throws Exception {
        byte[] serializedLoginResponseState = SignalCrypto.decryptMessage(encryptedLoginState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        LoginResponseState state = (LoginResponseState) net.strangled.maladan.cli.Main.reconstructSerializedObject(serializedLoginResponseState);

        if (state.isValidLogin()) {
            StaticComms.setAuthResults(new AuthResults("Logged In Successfully", true));
        } else {
            StaticComms.setAuthResults(new AuthResults("Failed to Login.", false));
        }
    }

    private void returnRegistrationResults(EncryptedRegistrationState encryptedRegistrationState) throws Exception {
        byte[] serializedRegistrationResponseState = SignalCrypto.decryptMessage(encryptedRegistrationState.getEncryptedState(), new SignalProtocolAddress("SERVER", 0));
        RegistrationResponseState state = (RegistrationResponseState) net.strangled.maladan.cli.Main.reconstructSerializedObject(serializedRegistrationResponseState);
        boolean loginState = state.isValidRegistration();

        if (loginState) {
            StaticComms.setAuthResults(new AuthResults("Successfully Registered.", true));
        } else {
            StaticComms.setAuthResults(new AuthResults("Registration Failed.", false));
        }
    }

    private void handleRequestedUserPreKeyBundle(ServerResponsePreKeyBundle bundle) {
        StaticComms.setUserBundle(bundle.getPreKeyBundle());
    }

    private void handleIncomingMMessage(MMessageObject object) {
        StaticComms.addIncomingMessage(object);
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

}
