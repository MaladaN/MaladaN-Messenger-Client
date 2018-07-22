package net.strangled.maladan.serializables.Authentication;

public class EncryptedClientPreKeyBundle implements java.io.Serializable, IEncryptedAuth {
    //stores an encrypted client pre key bundle for transport

    private byte[] encryptedSerializedClientPreKeyBundle;

    @Override
    public void storeEncryptedData(byte[] encryptedData) {
        this.encryptedSerializedClientPreKeyBundle = encryptedData;
    }

    @Override
    public byte[] getEncryptedData() {
        return this.encryptedSerializedClientPreKeyBundle;
    }
}
