package net.strangled.maladan;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.MaladaN.Tor.thoughtcrime.InitData;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.Destination;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Main extends Application {
    private static Stage stage;
    private static InitData data = null;

    public static void main(String[] args) {
        launch(args);
    }

    static InitData getData() {
        return data;
    }

    static byte[] hashData(String data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(data.getBytes());
        return messageDigest.digest();
    }

    static byte[] serializeObject(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(object);
        out.flush();
        return bos.toByteArray();
    }

    static Object reconstructSerializedObject(byte[] object) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(object);
        ObjectInput in = new ObjectInputStream(bis);
        return in.readObject();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("Splash.fxml"));
        primaryStage.setTitle("Connecting...");
        primaryStage.setScene(new Scene(root, 375, 375));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("file:./ico/Exposed_Conscious.png"));

        Thread init = new Thread(() -> {

            Thread t1 = new Thread(() -> {
                try {
                    data = SignalCrypto.initStore();
                    I2PSocketManager manager = I2PSocketManagerFactory.createManager();
                    Destination destination = new Destination("JaiCQHfweWn8Acp1XyTse1GL1392f-ZKzal9kyOhBAo-oYtnXAJIe8JU73taAjROnWApCe-hRUOlb6RkwW3kL2orqR8zhO6RDQMmOMy7FYqCq3UlNOOEQbLO1wo3kd65PA8D1zkhdFYqfYsQk4uEgci4~bamadKNOJXE1C~A53kEY-kYQ-vRSdV9LSFCRGay5BNDVJ1lFI~CYJRmreMx1hvd9YAsUg0fuy-U0AzylXwigSRejBhCNfsF-6-dLCQa8KYg8gzxe0DHUNRw18Yf1VwnvV7X2gM0CRQVcMhu7YgD3iwfT~DKFjZqRbNse~xEF0RtMCfhg7LgyCBRlJGVTj2PeXgxVtWHm3L-BtZ4bB5Ugb6K3ZdUFq9zP~VyKUmUJXSpApqhGdiGUWjj91-OZDJYnh6xgT17i-g0T2tEYLoSx9em~YZQQ~-mO3iSpiccSvmPjOpg9X1XVp9QvIyvWQIwrkv6y6ZgHeTrsxsG8HBZhPbMy6flinJRsCcPnIOlAAAA");
                    I2PSocket sock = manager.connect(destination);
                    new OutgoingMessageThread(sock.getOutputStream()).start();
                    new IncomingMessageThread(sock.getInputStream()).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t1.run();

            try {
                t1.join();

                Runnable runLater = this::startLoginStage;

                Platform.runLater(runLater);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        init.start();
        primaryStage.show();

    }

    @Override
    public void stop() throws Exception {
        OutgoingMessageThread.running = false;
        IncomingMessageThread.running = false;
    }

    private void startLoginStage() {
        try {
            stage.close();
            Parent root = FXMLLoader.load(getClass().getResource("SignIn.fxml"));
            stage.setTitle("Log In Or Register");
            stage.setScene(new Scene(root, 375, 500));
            stage.setResizable(false);
            stage.getIcons().add(new Image("file:./ico/Exposed_Conscious.png"));

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
