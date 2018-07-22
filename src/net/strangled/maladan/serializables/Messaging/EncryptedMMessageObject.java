package net.strangled.maladan.serializables.Messaging;

public class EncryptedMMessageObject implements java.io.Serializable, IEncryptedMessage {
    //Stores encrypted MMessageObject class

    private byte[] encryptedSerializedMMessageObject;

    @Override
    public void storeEncryptedMessage(byte[] message) {
        this.encryptedSerializedMMessageObject = message;
    }

    @Override
    public byte[] getEncryptedMessage() {
        return this.encryptedSerializedMMessageObject;
    }
}
