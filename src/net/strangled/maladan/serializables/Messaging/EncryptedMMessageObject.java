package net.strangled.maladan.serializables.Messaging;

public class EncryptedMMessageObject implements java.io.Serializable {
    //Stores encrypted MMessageObject class

    private byte[] encryptedSerializedMMessageObject;

    public EncryptedMMessageObject(byte[] encryptedSerializedMMessageObject) {
        this.encryptedSerializedMMessageObject = encryptedSerializedMMessageObject;
    }

    public byte[] getEncryptedSerializedMMessageObject() {
        return encryptedSerializedMMessageObject;
    }
}
