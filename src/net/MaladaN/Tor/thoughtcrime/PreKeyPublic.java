package net.MaladaN.Tor.thoughtcrime;


import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPublicKey;

public class PreKeyPublic implements java.io.Serializable {
    private byte[] preKeyPublic;
    private int prekeyId;

    public PreKeyPublic(ECPublicKey preKeyPublic, int prekeyId) {
        this.preKeyPublic = preKeyPublic.serialize();
        this.prekeyId = prekeyId;
    }

    public ECPublicKey getPreKeyPublic() {
        ECPublicKey publicKey = null;
        try {
            publicKey = Curve.decodePoint(preKeyPublic, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public int getPrekeyId() {
        return prekeyId;
    }
}
