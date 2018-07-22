package net.strangled.maladan.serializables.Messaging;

public class EncryptedFileInitiation implements java.io.Serializable, IEncryptedMessage {

    private byte[] serializedEncryptedFileInitiation;

    @Override
    public void storeEncryptedMessage(byte[] message) {
        this.serializedEncryptedFileInitiation = message;
    }

    @Override
    public byte[] getEncryptedMessage() {
        return serializedEncryptedFileInitiation;
    }
}
