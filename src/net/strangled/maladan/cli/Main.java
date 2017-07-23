package net.strangled.maladan.cli;


import net.MaladaN.Tor.thoughtcrime.InitData;
import net.MaladaN.Tor.thoughtcrime.MMessageObject;
import net.MaladaN.Tor.thoughtcrime.MySignalProtocolStore;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.Destination;
import net.strangled.maladan.serializables.*;
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
            System.out.println("Enter the username of the user you would like to converse with: ");
            String username = input.nextLine();
            while (running) {
                System.out.println("Enter a message to send: ");
                String messageToSend = input.nextLine();
                boolean sent = sendStringMessage(messageToSend, username);
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
            Destination destination = new Destination("3UpJSG4KBkiCCZ~85mT3xo888jRWMsU6WTkV03YkIwotcUYVfC5QuKgwwzUPPCQCYfe66k5nHX2FMzLmPLLE7NFjSotPhlU5HbFW0d0rt6yePDZ13j1gNKhwPP8OtdDgwEn6w-3kxeeg6XVGmI7qCnMLTJMlICVp4jwdGPcelMfYPswELdLQO54q2IW~4RsiGXuWn4evKeAp7R6c-Ys6H5LWPWyIfbB9XajGohntRbpAW2xpfSVfTTwYS1UIvxfQL~Ped4j909CTnZjFpaLAe4mOekIZltokShGWD2IPfKUuLpcUsf6cw-ThuoI5cJrVKXG2RKpwuqAyJlnMuEFN7WhTskeVlZQXWcJ615uB09uvxhMtSf3s3QQw8Iu-2rsD~FS44xy35cCVCwQ-WNQMsbuq9MNP-A3vH4~428dfab6TtYyrVamac7F228jvkdFQ7rAM9Q-Ju0Se6y19eceqQ10Cd-e6VZnb4XZeERSE8vYfFOcK5M5zVp7KV1ij7wPAAAAA");
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
            User sendUser = new User(false, actualUsername);
            byte[] serializedUser = serializeObject(sendUser);
            byte[] encryptedSerializedUser = SignalCrypto.encryptByteMessage(serializedUser, new SignalProtocolAddress("SERVER", 0), null);

            EncryptedUser encryptedUser = new EncryptedUser(encryptedSerializedUser);
            StaticComms.addOutgoingMessage(encryptedUser);

            while (StaticComms.getUserBundle() == null) {
                Thread.sleep(600);
            }
            requestedUserBundle = StaticComms.getUserBundle();
            StaticComms.setUserBundle(null);
        }

        byte[] eteeMessage;
        if (requestedUserBundle != null) {
            eteeMessage = SignalCrypto.encryptStringMessage(message, new SignalProtocolAddress(actualUsername, 0), requestedUserBundle);

        } else {
            eteeMessage = SignalCrypto.encryptStringMessage(message, new SignalProtocolAddress(actualUsername, 0), null);
        }

        if (eteeMessage != null) {
            User user = LocalLoginDataStore.getData();

            if (user != null) {
                String ourUsername = user.getUsername();
                MMessageObject mMessageObject = new MMessageObject(eteeMessage, actualUsername, ourUsername);

                byte[] serializedMMessageObject = Main.serializeObject(mMessageObject);
                byte[] encryptedSerializedMessageObject = SignalCrypto.encryptByteMessage(serializedMMessageObject, new SignalProtocolAddress("SERVER", 0), null);
                EncryptedMMessageObject encryptedMMessageObject = new EncryptedMMessageObject(encryptedSerializedMessageObject);

                StaticComms.addOutgoingMessage(encryptedMMessageObject);
                return true;
            }
            return false;

        }
        return false;
    }

    boolean sendFileMessage(File file, String recipientUsername) {
        return false;
    }
}
