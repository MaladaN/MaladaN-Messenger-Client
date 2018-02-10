package net.strangled.maladan.serializables.Messaging;

public class FileInitiation implements java.io.Serializable {

    private long fileLengthInBytes;
    private String base64Username;
    private String destinationBase64Username;

    private byte[] encryptedInitialFileBuffer;

    public FileInitiation(long fileLengthInBytes, String base64Username, String destinationBase64Username, byte[] encryptedInitialFileBuffer) {
        this.fileLengthInBytes = fileLengthInBytes;
        this.base64Username = base64Username;
        this.destinationBase64Username = destinationBase64Username;
        this.encryptedInitialFileBuffer = encryptedInitialFileBuffer;
    }

    public long getFileLengthInBytes() {
        return fileLengthInBytes;
    }

    public String getBase64Username() {
        return base64Username;
    }

    public String getDestinationBase64Username() {
        return destinationBase64Username;
    }

    public byte[] getEncryptedInitialFileBuffer() {
        return encryptedInitialFileBuffer;
    }
}
