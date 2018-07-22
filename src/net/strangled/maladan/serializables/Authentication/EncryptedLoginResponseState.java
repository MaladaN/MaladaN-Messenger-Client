package net.strangled.maladan.serializables.Authentication;

public class EncryptedLoginResponseState implements java.io.Serializable, IEncryptedAuth {
    //Holds the login State for transport

    private byte[] encryptedState;

    @Override
    public void storeEncryptedData(byte[] encryptedData) {
        this.encryptedState = encryptedData;
    }

    @Override
    public byte[] getEncryptedData() {
        return encryptedState;
    }
}
