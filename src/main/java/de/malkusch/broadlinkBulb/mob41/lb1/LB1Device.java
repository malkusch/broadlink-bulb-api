package de.malkusch.broadlinkBulb.mob41.lb1;

import static de.malkusch.broadlinkBulb.mob41.lb1.State.EMPTY;
import static java.util.Objects.hash;

import java.io.IOException;
import java.time.Duration;

import com.github.mob41.blapi.BLDevice;
import com.github.mob41.blapi.mac.Mac;

public final class LB1Device extends BLDevice {

    private final String mac;
    private final String host;
    private final Duration timeout;
    private final Codec codec;
    public static final short DEVICE_TYPE = -56;

    public LB1Device(String host, String mac, Duration timeout, Codec codec) throws IOException {
        super(DEVICE_TYPE, "Smart bulb LB1", host, reverseMac(mac));

        this.mac = mac;
        this.host = host;
        this.timeout = timeout;
        this.codec = codec;
    }

    public void changeState(State state) throws IOException {
        var changed = send(state, FLAG_WRITE).toMap();

        for (var entry : state.toMap().entrySet()) {
            var key = entry.getKey();
            var expected = entry.getValue();
            if (expected == null) {
                continue;
            }
            var actual = changed.get(key);

            if (!expected.equals(actual)) {
                throw new IOException(
                        String.format("%s failed changing %s to %s, was %s", this, key, expected, actual));
            }
        }
    }

    public State readState() throws IOException {
        return send(EMPTY, FLAG_READ);
    }

    private static final int FLAG_WRITE = 2;
    private static final int FLAG_READ = 1;

    private State send(State state, int flag) throws IOException {
        var cmd = codec.encode(state, flag);
        var response = sendCmdPkt((int) timeout.toMillis(), cmd);
        var encrypted = response.getData();

        int err = encrypted[0x22] | (encrypted[0x23] << 8);
        if (err != 0) {
            throw new IOException(
                    String.format("%s received returned err: %s/%d", this, Integer.toHexString(err), err));
        }

        byte[] decrypted;
        try {
            decrypted = decryptFromDeviceMessage(encrypted);
        } catch (Exception e) {
            throw new IOException(String.format("%s can't decrypt response", this), e);
        }
        return codec.decode(decrypted);
    }

    public String mac() {
        return mac;
    }

    public String host() {
        return host;
    }

    @Override
    public int hashCode() {
        return hash(mac, host);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LB1Device)) {
            return false;
        }
        var otherDevice = (LB1Device) other;
        return host.equals(otherDevice.host) && mac.equals(otherDevice.mac);
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", host, mac);
    }

    private static Mac reverseMac(String mac) {
        var bytes = Mac.macStrToBytes(mac);
        var reversed = new byte[6];
        reversed[0] = bytes[5];
        reversed[1] = bytes[4];
        reversed[2] = bytes[3];
        reversed[3] = bytes[2];
        reversed[4] = bytes[1];
        reversed[5] = bytes[0];

        return new Mac(reversed);
    }
}
