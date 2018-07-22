package net.strangled.maladan.serializables.Messaging;

public interface IEncryptedMessage {

    void storeEncryptedMessage(byte[] message);

    byte[] getEncryptedMessage();
}
