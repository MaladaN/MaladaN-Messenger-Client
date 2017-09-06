package net.strangled.maladan.cli;


import net.MaladaN.Tor.thoughtcrime.SignalCrypto;
import net.strangled.maladan.serializables.Messaging.MMessageObject;
import net.strangled.maladan.shared.StaticComms;
import org.whispersystems.libsignal.SignalProtocolAddress;

import java.util.List;

public class HandleMessage implements Runnable {

    private boolean running = true;
    private Thread t;

    @Override
    public void run() {
        while (running) {
            List<MMessageObject> objects;

            try {
                Thread.sleep(600);
            } catch (Exception e) {
                e.printStackTrace();
            }

            objects = StaticComms.getIncomingMessages();

            if (!objects.isEmpty()) {

                for (MMessageObject object : objects) {
                    byte[] encryptedMessage = object.getSerializedMessageObject();
                    String decryptedMessage = SignalCrypto.decryptStringMessage(encryptedMessage, new SignalProtocolAddress(object.getSendingUser(), 0));
                    System.out.println(decryptedMessage);
                }

                StaticComms.deleteMessageObjects(objects);
            }

        }
    }

    void shutdown() {
        running = false;
    }

    void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }
}
