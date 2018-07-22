package net.strangled.maladan.serializables.Messaging;

public class EncryptedFileEnd implements java.io.Serializable, IEncryptedMessage {

    private byte[] serializedEncryptedFileEnd;

    @Override
    public void storeEncryptedMessage(byte[] message) {
        this.serializedEncryptedFileEnd = message;
    }

    @Override
    public byte[] getEncryptedMessage() {
        return this.serializedEncryptedFileEnd;
    }
}
