package net.strangled.maladan.serializables.Messaging;

public class FileSpan implements java.io.Serializable {

    private String destinationBase64Username;
    private byte[] encryptedFileBuffer;

    public FileSpan(String destinationBase64Username, byte[] encryptedFileBuffer) {
        this.destinationBase64Username = destinationBase64Username;
        this.encryptedFileBuffer = encryptedFileBuffer;
    }

    public String getDestinationBase64Username() {
        return destinationBase64Username;
    }

    public byte[] getEncryptedFileBuffer() {
        return encryptedFileBuffer;
    }
}
