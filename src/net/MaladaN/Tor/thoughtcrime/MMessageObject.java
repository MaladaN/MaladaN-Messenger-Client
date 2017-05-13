package net.MaladaN.Tor.thoughtcrime;

public class MMessageObject implements java.io.Serializable {
    private byte[] serializedMessageObject;

    public MMessageObject(byte[] serializedMessageObject) {
        this.serializedMessageObject = serializedMessageObject;
    }

    public byte[] getSerializedMessageObject() {
        return serializedMessageObject;
    }
}
