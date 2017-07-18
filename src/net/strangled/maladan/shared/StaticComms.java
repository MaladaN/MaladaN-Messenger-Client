package net.strangled.maladan.shared;


import net.MaladaN.Tor.thoughtcrime.MMessageObject;
import net.strangled.maladan.serializables.AuthResults;
import org.whispersystems.libsignal.state.PreKeyBundle;

import java.util.List;
import java.util.Vector;

public class StaticComms {

    private static String password = "";
    private static String username = "";
    private static AuthResults loginResults = null;
    private static PreKeyBundle userBundle = null;
    private static Vector<MMessageObject> incomingMessages = new Vector<>();
    private static Vector<Object> outgoingMessages = new Vector<>();
    private static boolean registrationFlag;

    public static synchronized void setCredentials(String password, String username) {
        StaticComms.password = password;
        StaticComms.username = username;
        registrationFlag = true;
    }

    public static synchronized String getPassword() {
        return password;
    }

    public static synchronized String getUsername() {
        return username;
    }

    public static synchronized void clearLoginData() {
        StaticComms.password = "";
        StaticComms.username = "";
    }

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

    public static synchronized boolean isRegistrationFlag() {
        return registrationFlag;
    }

    public static synchronized void falsifyRegistrationFlag() {
        StaticComms.registrationFlag = false;
    }

    public static synchronized List<MMessageObject> getIncomingMessages() {
        Vector<MMessageObject> objects = new Vector<>();
        objects.addAll(incomingMessages);
        return objects;
    }

    public static synchronized void addIncomingMessage(MMessageObject object) {
        StaticComms.incomingMessages.add(object);
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

    public static synchronized void deleteMessageObjects(List<MMessageObject> objectsToRemove) {
        StaticComms.incomingMessages.removeAll(objectsToRemove);
    }
}
