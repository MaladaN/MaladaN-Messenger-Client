package net.strangled.maladan.serializables.Messaging;

public class MMessageObject implements java.io.Serializable {
    private byte[] serializedMessageObject;
    private String destinationUser;
    private String sendingUser;

    public MMessageObject(byte[] serializedMessageObject, String destinationUser, String sendingUser) {
        this.serializedMessageObject = serializedMessageObject;
        this.destinationUser = destinationUser;
        this.sendingUser = sendingUser;
    }

    public byte[] getSerializedMessageObject() {
        return serializedMessageObject;
    }

    public String getDestinationUser() {
        return destinationUser;
    }

    public String getSendingUser() {
        return sendingUser;
    }
}
