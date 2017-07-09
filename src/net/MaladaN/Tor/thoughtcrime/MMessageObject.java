package net.MaladaN.Tor.thoughtcrime;

public class MMessageObject implements java.io.Serializable {
    private byte[] serializedMessageObject;
    private String destinationUser;

    public MMessageObject(byte[] serializedMessageObject, String destinationUser) {
        this.serializedMessageObject = serializedMessageObject;
        this.destinationUser = destinationUser;
    }

    public byte[] getSerializedMessageObject() {
        return serializedMessageObject;
    }

    public String getDestinationUser() {
        return destinationUser;
    }
}
