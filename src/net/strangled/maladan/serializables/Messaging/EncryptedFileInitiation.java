package net.strangled.maladan.serializables.Messaging;

public class EncryptedFileInitiation implements java.io.Serializable {

    private byte[] serializedEncryptedFileInitiation;

    public EncryptedFileInitiation(byte[] serializedEncryptedFileInitiation) {
        this.serializedEncryptedFileInitiation = serializedEncryptedFileInitiation;
    }

    public byte[] getSerializedEncryptedFileInitiation() {
        return serializedEncryptedFileInitiation;
    }
}
