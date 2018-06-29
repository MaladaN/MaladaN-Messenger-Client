package net.strangled.maladan.cli;


import net.MaladaN.Tor.thoughtcrime.InitData;
import net.MaladaN.Tor.thoughtcrime.MySignalProtocolStore;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.Destination;
import net.strangled.maladan.serializables.Authentication.EncryptedUser;
import net.strangled.maladan.serializables.Authentication.ServerInit;
import net.strangled.maladan.serializables.Authentication.ServerLogin;
import net.strangled.maladan.serializables.Authentication.User;
import net.strangled.maladan.serializables.Messaging.*;
import net.strangled.maladan.shared.IncomingMessageThread;
import net.strangled.maladan.shared.LocalLoginDataStore;
import net.strangled.maladan.shared.OutgoingMessageThread;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyBundle;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static boolean running = true;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String host;
        int port;

        System.out.println("Enter the host and port of the configured i2p router. If you are running the i2p router on this machine, these can be left blank.");
        System.out.print("Host: ");
        host = input.nextLine();
        System.out.print("Port: ");
        port = input.nextInt();
        input.nextLine();
        connect(host, port);
        System.out.println("Connected.");

        MessageHandlerThread messageHandlerThread = new MessageHandlerThread();
        messageHandlerThread.start();

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

            System.out.println("Enter a filename to test file encryption.");
            String filename = input.nextLine();
            boolean successful = sendFileMessage(filename, username);

            if (successful) {
                System.out.println("Successfully encrypted and saved.");
            } else {
                System.out.println("Something went wrong.");
            }

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

    //hash strings and return bytes using the SHA-256 digest
    public static byte[] hashData(String data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(data.getBytes());
        return messageDigest.digest();
    }

    public static byte[] serializeObject(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        }
    }

    public static Object reconstructSerializedObject(byte[] object) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(object)) {
            ObjectInput in = new ObjectInputStream(bis);
            return in.readObject();
        }
    }

    //takes string username passed in by the user and turns it into user's actual
    //base64 representation of their byte username.
    public static String getActualUsername(String originalUsername) throws Exception {
        byte[] hashedUsername = hashData(originalUsername);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(hashedUsername);
    }

    //takes the base64 username of a possible message recipient and checks if we have
    //a valid signal messaging session. If not we request a pre key bundle for the
    //recipient user from the server. If a session already exists the method returns null.

    //when the SignalCrypto library receives a prekey bundle of null it assumes that a session
    //exists in our database and attempts to encrypt using that session.
    private static PreKeyBundle getPreKeyBundleIfNoSession(String base64Username) throws Exception {
        boolean sessionExists = new MySignalProtocolStore().containsSession(new SignalProtocolAddress(base64Username, 0));
        PreKeyBundle requestedUserBundle = null;

        if (!sessionExists) {
            User sendUser = new User(false, base64Username);
            byte[] serializedUser = serializeObject(sendUser);
            byte[] encryptedSerializedUser = SignalCrypto.encryptByteMessage(serializedUser, new SignalProtocolAddress("SERVER", 0), null);

            EncryptedUser encryptedUser = new EncryptedUser(encryptedSerializedUser);
            OutgoingMessageThread.addOutgoingMessage(encryptedUser);

            while (IncomingMessageThread.getUserBundle() == null) {
                Thread.sleep(600);
            }

            requestedUserBundle = IncomingMessageThread.getUserBundle();
            IncomingMessageThread.setUserBundle(null);
        }
        return requestedUserBundle;
    }

    static I2PSocket connect(String host, int port) {
        I2PSocket sock;
        I2PSocketManager manager;
        try {

            if (!(host.equals("")) && port != 0) {
                manager = I2PSocketManagerFactory.createManager(host, port);
            } else {
                manager = I2PSocketManagerFactory.createManager();
            }

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

        //public key retrieved to verify that the server has the same one stored.
        //if the user changes their key data they won't be able to login.
        IdentityKey key = new MySignalProtocolStore().getIdentityKeyPair().getPublicKey();

        byte[] serializedKey = key.serialize();

        if (user != null) {
            String username = user.getUsername();

            byte[] encryptedPassword = SignalCrypto.encryptStringMessage(password, address, null);

            ServerLogin login = new ServerLogin(username, encryptedPassword, serializedKey);

            OutgoingMessageThread.addOutgoingMessage(login);

            return waitForAuthData();

        } else {
            return null;
        }
    }

    static AuthResults register(String username, String password, String uniqueId) throws Exception {
        if (username.isEmpty() || password.isEmpty() || uniqueId.isEmpty()) {
            return null;
        }

        InitData data = SignalCrypto.initStore();

        String actualUsername = getActualUsername(username);
        ServerInit init = new ServerInit(actualUsername, uniqueId, data);

        IncomingMessageThread.setCredentials(password, username);
        OutgoingMessageThread.addOutgoingMessage(init);

        LocalLoginDataStore.saveLocaluser(new User(true, actualUsername));

        return waitForAuthData();
    }

    //used by the login and register method to wait for a response
    //from the server.
    private static AuthResults waitForAuthData() throws Exception {
        while (IncomingMessageThread.getAuthResults() == null) {
            Thread.sleep(1000);
        }

        AuthResults results = IncomingMessageThread.getAuthResults();
        IncomingMessageThread.clearAuthResults();

        return results;
    }

    static boolean sendStringMessage(String message, String recipientUsername) throws Exception {
        String actualRecipientUsername = getActualUsername(recipientUsername);
        PreKeyBundle requestedUserBundle = getPreKeyBundleIfNoSession(actualRecipientUsername);

        byte[] eteeMessage = SignalCrypto.encryptStringMessage(message, new SignalProtocolAddress(actualRecipientUsername, 0), requestedUserBundle);

        if (eteeMessage != null) {
            User user = LocalLoginDataStore.getData();

            if (user != null) {
                String ourUsername = user.getUsername();
                MMessageObject mMessageObject = new MMessageObject(eteeMessage, actualRecipientUsername, ourUsername);

                byte[] serializedMMessageObject = Main.serializeObject(mMessageObject);
                byte[] encryptedSerializedMessageObject = SignalCrypto.encryptByteMessage(serializedMMessageObject, new SignalProtocolAddress("SERVER", 0), null);
                EncryptedMMessageObject encryptedMMessageObject = new EncryptedMMessageObject(encryptedSerializedMessageObject);

                OutgoingMessageThread.addOutgoingMessage(encryptedMMessageObject);
                return true;
            }
            return false;

        }
        return false;
    }

    static boolean sendFileMessage(String fileName, String recipientUsername) throws Exception {

        //TODO continue File sending procedure
        //TODO next step after containers: add handlers for each new class coming into the server.
        //TODO " figure out a system of storing the reconstructed file on the server in escrow until the recipient user is ready to receive it.
        //TODO " Send file to client using the same classes for sending to the server, and create reception handlers for the client to save the file in a preferred location on local disk.
        String actualRecipientUsername = getActualUsername(recipientUsername);
        String encryptedFilePath = encryptFile(fileName, actualRecipientUsername);

        try (InputStream in = new FileInputStream(encryptedFilePath)) {
            User user = LocalLoginDataStore.getData();

            if (user != null) {
                String ourUsername = user.getUsername();

                File temporaryFile = new File(encryptedFilePath);
                byte[] buffer = new byte[FileConstants.bufferLength];


                //handles the fact that the file will likely not exactly fit into the buffer sizes.
                //If the result of attempting to divide the length of the file by the buffer returns
                //a floating point value, the number of buffers is increased by one.

                float numberOfFileObjectsTemp = (float) temporaryFile.length() / (float) FileConstants.bufferLength;
                float subtractionValue = numberOfFileObjectsTemp % 1;
                boolean needToClean = !(subtractionValue == 0);


                if (needToClean) {
                    numberOfFileObjectsTemp -= subtractionValue;
                    numberOfFileObjectsTemp++;
                }

                int numberOfFileObjects = (int) numberOfFileObjectsTemp;
                System.out.println("number of objects to send: " + numberOfFileObjects);

                int numberOfFileObjectsIncrementer = numberOfFileObjects - 1;

                int i = 0;
                SignalProtocolAddress serverAddress = new SignalProtocolAddress("SERVER", 0);

                while (in.read(buffer) > 0) {
                    if (i == 0) {
                        FileInitiation fileStart = new FileInitiation(temporaryFile.length(), ourUsername, actualRecipientUsername, buffer);

                        byte[] serializedEncryptedFileStart = SignalCrypto.encryptByteMessage(Main.serializeObject(fileStart), serverAddress, null);
                        EncryptedFileInitiation eFI = new EncryptedFileInitiation(serializedEncryptedFileStart);

                        OutgoingMessageThread.addOutgoingMessage(eFI);
                        System.out.println("Added Encrypted File Initiation" + i + " to outThread.");

                    } else if (i == numberOfFileObjectsIncrementer) {
                        FileEnd fileEnd = new FileEnd(ourUsername, buffer);

                        byte[] serializedEncryptedFileEnd = SignalCrypto.encryptByteMessage(Main.serializeObject(fileEnd), serverAddress, null);
                        EncryptedFileEnd eFE = new EncryptedFileEnd(serializedEncryptedFileEnd);

                        OutgoingMessageThread.addOutgoingMessage(eFE);
                        System.out.println("Added Encrypted File End" + i + " to outThread.");

                    } else {
                        FileSpan fileSpan = new FileSpan(ourUsername, buffer);

                        byte[] serializedEncryptedFileSpan = SignalCrypto.encryptByteMessage(Main.serializeObject(fileSpan), serverAddress, null);
                        EncryptedFileSpan eFS = new EncryptedFileSpan(serializedEncryptedFileSpan);

                        OutgoingMessageThread.addOutgoingMessage(eFS);
                        System.out.println("Added Encrypted File Span" + i + " to outThread.");
                    }
                    i++;
                }

            } else {
                throw new Exception("Local user object is null.");
            }
        }

        File tempFile = new File(encryptedFilePath);
        tempFile.delete();

        return true;
    }

    //returns file path to new encrypted file.
    private static String encryptFile(String filePath, String actualRecipientUsername) throws Exception {
        File tempFile = new File("Files");
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }

        final String temporaryFileDirectory = "Files" + File.separator;
        final String uuid = UUID.randomUUID().toString();

        PreKeyBundle bundle = getPreKeyBundleIfNoSession(actualRecipientUsername);

        File fileToSend = new File(filePath);
        String temporaryFilename = temporaryFileDirectory + uuid + fileToSend.getName() + ".mal";

        try (InputStream in = new FileInputStream(fileToSend);
             OutputStream out = new FileOutputStream(temporaryFilename, true)) {

            byte[] buffer = new byte[FileConstants.encryptionBufferLength];

            //read in buffer of file, encrypt buffer, and write buffer to disk in new encrypted file.

            while ((in.read(buffer)) > 0) {
                byte[] encryptedBuffer = SignalCrypto.encryptByteMessage(buffer, new SignalProtocolAddress(actualRecipientUsername, 0), bundle);

                if (encryptedBuffer != null) {
                    out.write(encryptedBuffer);

                } else {
                    throw new Exception();
                }
            }
            out.flush();
        }
        return temporaryFilename;
    }
}
