package net.strangled.maladan.serializables.Messaging;

public class FileSpan {

    private String destinationBase64Username;

    private byte[] encryptedFileBuffer;

    public FileSpan(String destinationBase64Username, byte[] encryptedFileBuffer) {
        this.destinationBase64Username = destinationBase64Username;
        this.encryptedFileBuffer = encryptedFileBuffer;
    }
}
