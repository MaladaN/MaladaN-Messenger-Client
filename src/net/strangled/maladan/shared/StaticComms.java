package net.strangled.maladan.shared;


import net.strangled.maladan.cli.AuthResults;
import net.strangled.maladan.serializables.Messaging.MMessageObject;
import org.whispersystems.libsignal.state.PreKeyBundle;

import java.util.List;
import java.util.Vector;

public class StaticComms {

    private static AuthResults loginResults = null;

    private static PreKeyBundle userBundle = null;

    private static Vector<MMessageObject> incomingMessages = new Vector<>();

    private static Vector<Object> outgoingMessages = new Vector<>();


    public static synchronized AuthResults getAuthResults() {
        return loginResults;
    }

    public static synchronized void setAuthResults(AuthResults results) {
        StaticComms.loginResults = results;
    }

    public static synchronized void clearAuthResults() {
        StaticComms.loginResults = null;
    }


    public static synchronized PreKeyBundle getUserBundle() {
        return userBundle;
    }

    public static synchronized void setUserBundle(PreKeyBundle userBundle) {
        StaticComms.userBundle = userBundle;
    }


    public static synchronized List<MMessageObject> getIncomingMessages() {
        Vector<MMessageObject> objects = new Vector<>();
        objects.addAll(incomingMessages);
        return objects;
    }

    public static synchronized void addIncomingMessage(MMessageObject object) {
        StaticComms.incomingMessages.add(object);
    }

    public static synchronized void deleteMessageObjects(List<MMessageObject> objectsToRemove) {
        StaticComms.incomingMessages.removeAll(objectsToRemove);
    }


    public static synchronized void addOutgoingMessage(Object message) {
        outgoingMessages.add(message);
    }

    public static synchronized boolean outgoingMessagesStatus() {
        return outgoingMessages.isEmpty();
    }

    public static synchronized Vector<Object> getOutgoingMessages() {
        return new Vector<Object>() {{
            addAll(outgoingMessages);
        }};
    }

    public static synchronized void removeOutgoingMessages(List<Object> outgoingMessages) {
        StaticComms.outgoingMessages.removeAll(outgoingMessages);
    }
}
