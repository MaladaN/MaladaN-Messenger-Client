package net.MaladaN.Tor.thoughtcrime;


import java.io.*;

public class FileManagement {
    public static void writeFile(Object mPreKeyBundle, String place) throws IOException {
        FileOutputStream fos = new FileOutputStream(place);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(mPreKeyBundle);
        os.close();
        fos.close();
    }

    static void writeFile(byte[] alreadySerialized, String place) throws IOException {
        FileOutputStream fos = new FileOutputStream(place);
        fos.write(alreadySerialized);
        fos.close();
    }

    static Object readFile(String path) {
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object de_serialized = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            return de_serialized;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static byte[] readCipherTextMessage(String path) {
        try {
            File f = new File(path);
            byte fileContent[] = new byte[(int) f.length()];
            FileInputStream fileInputStream = new FileInputStream(path);
            fileInputStream.read(fileContent);
            return fileContent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
