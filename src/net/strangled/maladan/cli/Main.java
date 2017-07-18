package net.strangled.maladan.cli;


import net.MaladaN.Tor.thoughtcrime.InitData;
import net.MaladaN.Tor.thoughtcrime.MMessageObject;
import net.MaladaN.Tor.thoughtcrime.MySignalProtocolStore;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.Destination;
import net.strangled.maladan.serializables.AuthResults;
import net.strangled.maladan.serializables.ServerInit;
import net.strangled.maladan.serializables.ServerLogin;
import net.strangled.maladan.serializables.User;
import net.strangled.maladan.shared.IncomingMessageThread;
import net.strangled.maladan.shared.LocalLoginDataStore;
import net.strangled.maladan.shared.OutgoingMessageThread;
import net.strangled.maladan.shared.StaticComms;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyBundle;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Main {
    private static boolean running = true;

    public static void main(String[] args) {

        System.out.println("Connecting to the server...");
        connect();
        System.out.println("Connected.");

        HandleMessage handleMessage = new HandleMessage();
        handleMessage.start();

        Scanner input = new Scanner(System.in);
        System.out.println("Enter an option: register or login");
        String received = input.nextLine();
        received = received.toLowerCase();

        if (received.equals("register")) {
            System.out.println("Enter your username: ");
            String username = input.nextLine();
            username = username.toLowerCase();
            System.out.println("Enter your password: ");
            String password = input.nextLine();

            try {
                AuthResults results = register(username, password, "tester123");

                if (results != null) {
                    System.out.println(results.getFormattedResults());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Enter your password: ");
            String password = input.nextLine();

            try {
                AuthResults results = login(password);

                if (results != null) {
                    System.out.println(results.getFormattedResults());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            while (running) {
                System.out.println("Enter a message to send: ");
                String messageToSend = input.nextLine();
                boolean sent = sendStringMessage(messageToSend, "tester");
                if (sent) {
                    System.out.println("Message Sent");
                } else {
                    System.out.println("Message failed to send");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static byte[] hashData(String data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(data.getBytes());
        return messageDigest.digest();
    }

    public static byte[] serializeObject(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(object);
        out.flush();
        return bos.toByteArray();
    }

    public static Object reconstructSerializedObject(byte[] object) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(object);
        ObjectInput in = new ObjectInputStream(bis);
        return in.readObject();
    }

    static I2PSocket connect() {
        I2PSocket sock;

        try {
            I2PSocketManager manager = I2PSocketManagerFactory.createManager();
            Destination destination = new Destination("JaiCQHfweWn8Acp1XyTse1GL1392f-ZKzal9kyOhBAo-oYtnXAJIe8JU73taAjROnWApCe-hRUOlb6RkwW3kL2orqR8zhO6RDQMmOMy7FYqCq3UlNOOEQbLO1wo3kd65PA8D1zkhdFYqfYsQk4uEgci4~bamadKNOJXE1C~A53kEY-kYQ-vRSdV9LSFCRGay5BNDVJ1lFI~CYJRmreMx1hvd9YAsUg0fuy-U0AzylXwigSRejBhCNfsF-6-dLCQa8KYg8gzxe0DHUNRw18Yf1VwnvV7X2gM0CRQVcMhu7YgD3iwfT~DKFjZqRbNse~xEF0RtMCfhg7LgyCBRlJGVTj2PeXgxVtWHm3L-BtZ4bB5Ugb6K3ZdUFq9zP~VyKUmUJXSpApqhGdiGUWjj91-OZDJYnh6xgT17i-g0T2tEYLoSx9em~YZQQ~-mO3iSpiccSvmPjOpg9X1XVp9QvIyvWQIwrkv6y6ZgHeTrsxsG8HBZhPbMy6flinJRsCcPnIOlAAAA");
            sock = manager.connect(destination);

            System.out.println("Creating Threads...");
            new OutgoingMessageThread(sock.getOutputStream()).start();
            new IncomingMessageThread(sock.getInputStream()).start();
            System.out.println("Threads Created.");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sock;
    }

    static AuthResults login(String password) throws Exception {
        if (password.isEmpty()) {
            return null;
        }

        SignalProtocolAddress address = new SignalProtocolAddress("SERVER", 0);

        User user = LocalLoginDataStore.getData();

        IdentityKey key = new MySignalProtocolStore().getIdentityKeyPair().getPublicKey();
        byte[] serializedKey = key.serialize();

        if (user != null) {
            String username = user.getUsername();

            byte[] hashedUsername = hashData(username);
            String base64Username = DatatypeConverter.printBase64Binary(hashedUsername);

            byte[] hashedPassword = hashData(password);
            byte[] encryptedPassword = SignalCrypto.encryptByteMessage(hashedPassword, address, null);

            // TODO encrypt with server signal session
            ServerLogin login = new ServerLogin(base64Username, encryptedPassword, serializedKey);

            StaticComms.addOutgoingMessage(login);

            return waitForData();

        } else {
            return null;
        }
    }

    static AuthResults register(String username, String password, String uniqueId) throws Exception {
        if (username.isEmpty() || password.isEmpty() || uniqueId.isEmpty()) {
            return null;
        }

        InitData data = SignalCrypto.initStore();

        byte[] hashedUsername = hashData(username);
        ServerInit init = new ServerInit(hashedUsername, uniqueId, data);

        StaticComms.setCredentials(password, username);
        StaticComms.addOutgoingMessage(init);

        LocalLoginDataStore.saveLocaluser(new User(true, username));

        return waitForData();
    }

    private static AuthResults waitForData() throws Exception {
        while (StaticComms.getAuthResults() == null) {
            Thread.sleep(1000);
        }

        AuthResults results = StaticComms.getAuthResults();
        StaticComms.clearAuthResults();

        return results;
    }

    static boolean sendStringMessage(String message, String recipientUsername) throws Exception {
        byte[] hashedUsername = hashData(recipientUsername);
        String actualUsername = DatatypeConverter.printBase64Binary(hashedUsername);
        boolean sessionExists = new MySignalProtocolStore().containsSession(new SignalProtocolAddress(actualUsername, 0));
        PreKeyBundle requestedUserBundle = null;

        if (!sessionExists) {
            // TODO encrypt with server signal session
            User sendUser = new User(false, actualUsername);
            StaticComms.addOutgoingMessage(sendUser);

            while (StaticComms.getUserBundle() == null) {
                Thread.sleep(600);
            }
            requestedUserBundle = StaticComms.getUserBundle();
            StaticComms.setUserBundle(null);
        }
        // TODO fix efficiency of code. Copy and paste not good :/
        if (requestedUserBundle != null) {
            byte[] eteeMessage = SignalCrypto.encryptStringMessage(message, new SignalProtocolAddress(actualUsername, 0), requestedUserBundle);
            String ourUsername = LocalLoginDataStore.getData().getUsername();
            // TODO encrypt with server signal session
            MMessageObject mMessageObject = new MMessageObject(eteeMessage, actualUsername, ourUsername);
            StaticComms.addOutgoingMessage(mMessageObject);
            return true;

        } else {
            byte[] eteeMessage = SignalCrypto.encryptStringMessage(message, new SignalProtocolAddress(actualUsername, 0), null);
            String ourUsername = LocalLoginDataStore.getData().getUsername();
            // TODO encrypt with server signal session
            MMessageObject mMessageObject = new MMessageObject(eteeMessage, actualUsername, ourUsername);
            StaticComms.addOutgoingMessage(mMessageObject);
            return true;
        }
    }

    boolean sendFileMessage(File file, String recipientUsername) {
        return false;
    }
}
