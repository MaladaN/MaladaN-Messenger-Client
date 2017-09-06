package net.strangled.maladan.serializables.Authentication;

public class EncryptedRegistrationResponseState implements java.io.Serializable {
    private byte[] encryptedState;

    public EncryptedRegistrationResponseState(byte[] encryptedState) {
        this.encryptedState = encryptedState;
    }

    public byte[] getEncryptedState() {
        return encryptedState;
    }
}
