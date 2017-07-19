package net.strangled.maladan.serializables;


public class EncryptedLoginResponseState implements java.io.Serializable {
    //Holds the login State for transport

    private byte[] encryptedState;

    public EncryptedLoginResponseState(byte[] encryptedState) {
        this.encryptedState = encryptedState;
    }

    public byte[] getEncryptedState() {
        return encryptedState;
    }
}
