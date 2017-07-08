package net.strangled.maladan.cli;


import net.MaladaN.Tor.thoughtcrime.InitData;
import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.Destination;

import java.io.File;

public class Main {
    private static InitData data = null;

    public static void main(String[] args) {
        data = SignalCrypto.initStore();
    }

    I2PSocket connect() {
        I2PSocket sock;

        try {
            I2PSocketManager manager = I2PSocketManagerFactory.createManager();
            Destination destination = new Destination("JaiCQHfweWn8Acp1XyTse1GL1392f-ZKzal9kyOhBAo-oYtnXAJIe8JU73taAjROnWApCe-hRUOlb6RkwW3kL2orqR8zhO6RDQMmOMy7FYqCq3UlNOOEQbLO1wo3kd65PA8D1zkhdFYqfYsQk4uEgci4~bamadKNOJXE1C~A53kEY-kYQ-vRSdV9LSFCRGay5BNDVJ1lFI~CYJRmreMx1hvd9YAsUg0fuy-U0AzylXwigSRejBhCNfsF-6-dLCQa8KYg8gzxe0DHUNRw18Yf1VwnvV7X2gM0CRQVcMhu7YgD3iwfT~DKFjZqRbNse~xEF0RtMCfhg7LgyCBRlJGVTj2PeXgxVtWHm3L-BtZ4bB5Ugb6K3ZdUFq9zP~VyKUmUJXSpApqhGdiGUWjj91-OZDJYnh6xgT17i-g0T2tEYLoSx9em~YZQQ~-mO3iSpiccSvmPjOpg9X1XVp9QvIyvWQIwrkv6y6ZgHeTrsxsG8HBZhPbMy6flinJRsCcPnIOlAAAA");
            sock = manager.connect(destination);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sock;
    }

    boolean login(String username, String password) {
        return false;
    }

    boolean sendStringMessage(String message) {
        return false;
    }

    boolean sendFileMessage(File file) {
        return false;
    }


}
