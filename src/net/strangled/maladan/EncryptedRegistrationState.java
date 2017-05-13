package net.strangled.maladan;

public class EncryptedRegistrationState implements java.io.Serializable {
    private byte[] encryptedState;

    public EncryptedRegistrationState(byte[] encryptedState) {
        this.encryptedState = encryptedState;
    }

    public byte[] getEncryptedState() {
        return encryptedState;
    }
}
