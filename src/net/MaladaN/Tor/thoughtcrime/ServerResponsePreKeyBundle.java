package net.MaladaN.Tor.thoughtcrime;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.state.PreKeyBundle;

public class ServerResponsePreKeyBundle implements java.io.Serializable {
    //got this.
    private int registrationId;
    private PreKeyPublic preKey = null;
    private int signedPreKeyId;
    private byte[] signedPreKeyPublic;
    private byte[] signedPreKeySignature;
    private byte[] identityKey;

    //deviceId not present, only one device.

    public ServerResponsePreKeyBundle(int registrationId, PreKeyPublic preKey, int signedPreKeyId, byte[] signedPreKeyPublic, byte[] signedPreKeySignature, byte[] identityKey) {
        this.registrationId = registrationId;
        this.preKey = preKey;
        this.signedPreKeyId = signedPreKeyId;
        this.signedPreKeyPublic = signedPreKeyPublic;
        this.signedPreKeySignature = signedPreKeySignature;
        this.identityKey = identityKey;
    }

    public PreKeyBundle getPreKeyBundle() {
        try {
            return new PreKeyBundle(registrationId, 0, preKey.getPrekeyId(), preKey.getPreKeyPublic(), this.signedPreKeyId, Curve.decodePoint(this.signedPreKeyPublic, 0), this.signedPreKeySignature, new IdentityKey(identityKey, 0));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
