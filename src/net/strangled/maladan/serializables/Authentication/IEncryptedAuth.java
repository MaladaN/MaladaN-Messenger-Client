package net.strangled.maladan.serializables.Authentication;

public interface IEncryptedAuth {

    void storeEncryptedData(byte[] encryptedData);

    byte[] getEncryptedData();
}
