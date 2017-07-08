package net.strangled.maladan.gui;


import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Vector;

public class OutgoingMessageThread implements Runnable {

    static boolean running = true;
    private static Vector<Object> outgoingMessages = new Vector<>();
    private Thread t;
    private OutputStream stream;

    OutgoingMessageThread(OutputStream stream) {
        this.stream = stream;
    }

    static void addNewMessage(Object message) {
        outgoingMessages.add(message);
    }

    @Override
    public void run() {

        try {
            ObjectOutputStream out = new ObjectOutputStream(stream);

            while (running) {
                Thread.sleep(5);

                if (!outgoingMessages.isEmpty()) {
                    LinkedList<Object> currentOut = new LinkedList<>();
                    currentOut.addAll(outgoingMessages);

                    for (Object j : currentOut) {
                        out.writeObject(j);
                        Thread.sleep(2);
                        out.flush();
                        outgoingMessages.remove(j);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

}
