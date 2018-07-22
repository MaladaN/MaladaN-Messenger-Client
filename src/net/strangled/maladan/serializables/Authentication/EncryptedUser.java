package net.strangled.maladan.serializables.Authentication;

public class EncryptedUser implements java.io.Serializable, IEncryptedAuth {
    //Stored encrypted User class

    private byte[] encryptedSerializedUser;

    @Override
    public void storeEncryptedData(byte[] encryptedData) {
        this.encryptedSerializedUser = encryptedData;
    }

    @Override
    public byte[] getEncryptedData() {
        return this.encryptedSerializedUser;
    }
}
