package net.strangled.maladan;


public class ServerLogin implements java.io.Serializable {
    //Used by the client to login to the server

    private String EncodedHashedUsername;
    private byte[] encryptedPassword;

    public ServerLogin(String encodedHashedUsername, byte[] encryptedPassword) {
        EncodedHashedUsername = encodedHashedUsername;
        this.encryptedPassword = encryptedPassword;
    }

    public String getEncodedHashedUsername() {
        return EncodedHashedUsername;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }
}
