package net.strangled.maladan.serializables.Messaging;

public class FileEnd {

    private String destinationBase64Username;

    private byte[] encryptedFileBuffer;

    public FileEnd(String destinationBase64Username, byte[] encryptedFileBuffer) {
        this.destinationBase64Username = destinationBase64Username;
        this.encryptedFileBuffer = encryptedFileBuffer;
    }
}
