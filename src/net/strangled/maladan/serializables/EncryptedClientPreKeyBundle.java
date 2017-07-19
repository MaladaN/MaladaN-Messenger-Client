package net.strangled.maladan.serializables;

public class EncryptedClientPreKeyBundle implements java.io.Serializable {
    //stores an encrypted client pre key bundle for transport

    private byte[] encryptedSerializedClientPreKeyBundle;

    public EncryptedClientPreKeyBundle(byte[] encryptedSerializedClientPreKeyBundle) {
        this.encryptedSerializedClientPreKeyBundle = encryptedSerializedClientPreKeyBundle;
    }

    public byte[] getEncryptedSerializedClientPreKeyBundle() {
        return encryptedSerializedClientPreKeyBundle;
    }
}
