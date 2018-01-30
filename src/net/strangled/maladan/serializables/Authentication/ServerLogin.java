package net.strangled.maladan.serializables.Authentication;


public class ServerLogin implements java.io.Serializable {
    //Used by the client to login to the server

    private String username;
    private byte[] serializedIdentityKey;
    private byte[] encryptedPassword;

    public ServerLogin(String encodedHashedUsername, byte[] encryptedPassword, byte[] serializedIdentityKey) {
        username = encodedHashedUsername;
        this.encryptedPassword = encryptedPassword;
        this.serializedIdentityKey = serializedIdentityKey;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    public byte[] getSerializedIdentityKey() {
        return serializedIdentityKey;
    }
}
