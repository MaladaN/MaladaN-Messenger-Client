package net.strangled.maladan.serializables;


public class EncryptedLoginState implements java.io.Serializable {
    //Holds the login State for transport

    private byte[] encryptedState;

    public EncryptedLoginState(byte[] encryptedState) {
        this.encryptedState = encryptedState;
    }

    public byte[] getEncryptedState() {
        return encryptedState;
    }
}
