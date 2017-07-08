package net.strangled.maladan.serializables;


public class ServerLogin implements java.io.Serializable {
    //Used by the client to login to the server

    private String EncodedHashedUsername;
    private byte[] serializedIdentityKey;
    private byte[] encryptedPassword;

    public ServerLogin(String encodedHashedUsername, byte[] encryptedPassword, byte[] serializedIdentityKey) {
        EncodedHashedUsername = encodedHashedUsername;
        this.encryptedPassword = encryptedPassword;
        this.serializedIdentityKey = serializedIdentityKey;
    }

    public String getEncodedHashedUsername() {
        return EncodedHashedUsername;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    public byte[] getSerializedIdentityKey() {
        return serializedIdentityKey;
    }
}
