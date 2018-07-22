package net.strangled.maladan.serializables.Messaging;

public class EncryptedFileSpan implements java.io.Serializable, IEncryptedMessage {

    private byte[] serializedEncryptedFileSpan;

    @Override
    public void storeEncryptedMessage(byte[] message) {
        this.serializedEncryptedFileSpan = message;
    }

    @Override
    public byte[] getEncryptedMessage() {
        return this.serializedEncryptedFileSpan;
    }
}
