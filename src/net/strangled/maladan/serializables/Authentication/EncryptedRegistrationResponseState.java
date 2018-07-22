package net.strangled.maladan.serializables.Authentication;

public class EncryptedRegistrationResponseState implements java.io.Serializable, IEncryptedAuth {
    private byte[] encryptedState;

    @Override
    public void storeEncryptedData(byte[] encryptedData) {
        this.encryptedState = encryptedData;
    }

    @Override
    public byte[] getEncryptedData() {
        return this.encryptedState;
    }
}
