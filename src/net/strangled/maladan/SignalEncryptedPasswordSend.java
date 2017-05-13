package net.strangled.maladan;

public class SignalEncryptedPasswordSend implements java.io.Serializable {
    //Used to send the password for a new account, after it has been created using ServerInit.
    //Password comes in encrypted using the signal protocol.

    private byte[] serializedPassword;
    private String username;

    public SignalEncryptedPasswordSend(byte[] serializedPassword, String username) {
        this.serializedPassword = serializedPassword;
        this.username = username;
    }

    public byte[] getSerializedPassword() {
        return serializedPassword;
    }

    public String getUsername() {
        return username;
    }
}
