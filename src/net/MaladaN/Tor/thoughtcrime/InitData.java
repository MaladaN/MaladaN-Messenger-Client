package net.MaladaN.Tor.thoughtcrime;

import org.whispersystems.libsignal.state.PreKeyRecord;

import java.util.ArrayList;
import java.util.List;


public class InitData {
    private int signedPreKeyId;
    private List<PreKeyRecord> preKeyRecords;

    public InitData(int signedPreKeyId, List<PreKeyRecord> preKeyRecords) {
        saveData(signedPreKeyId, preKeyRecords);
    }

    public void saveData(int signedPreKeyId, List<PreKeyRecord> preKeyRecords) {
        if (signedPreKeyId != 0 && preKeyRecords != null) {
            this.signedPreKeyId = signedPreKeyId;
            this.preKeyRecords = preKeyRecords;
        }
    }

    public List<PreKeyRecord> getPreKeyRecords() {
        List<PreKeyRecord> records = new ArrayList<>();
        records.addAll(this.preKeyRecords);
        return records;
    }

    public int getSignedPreKeyId() {
        return this.signedPreKeyId;
    }
}
