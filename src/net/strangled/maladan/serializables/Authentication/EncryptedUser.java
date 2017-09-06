package net.strangled.maladan.serializables.Authentication;

public class EncryptedUser implements java.io.Serializable {
    //Stored encrypted User class

    private byte[] encryptedSerializedUser;

    public EncryptedUser(byte[] encryptedSerializedUser) {
        this.encryptedSerializedUser = encryptedSerializedUser;
    }

    public byte[] getEncryptedSerializedUser() {
        return encryptedSerializedUser;
    }
}
