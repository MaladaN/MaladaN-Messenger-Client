package net.strangled.maladan.shared;


import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class OutgoingMessageThread implements Runnable {

    public static boolean running = true;
    private Thread t;
    private OutputStream stream;

    public OutgoingMessageThread(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public void run() {

        try {
            ObjectOutputStream out = new ObjectOutputStream(stream);

            while (running) {
                Thread.sleep(5);

                if (!StaticComms.outgoingMessagesStatus()) {
                    LinkedList<Object> currentOut = new LinkedList<>();
                    currentOut.addAll(StaticComms.getOutgoingMessages());

                    for (Object j : currentOut) {
                        out.writeObject(j);
                        Thread.sleep(2);
                        out.flush();
                    }
                    StaticComms.removeOutgoingMessages(currentOut);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

}
