package de.malkusch.broadlinkLb2Api.mob41.lb2;

import java.io.IOException;

import com.github.mob41.blapi.BLDevice;
import com.github.mob41.blapi.mac.Mac;

public final class LB2Device extends BLDevice {

    private final Lb2StateCmdPayloadFactory commandFactory;

    private final String mac;
    private final String host;

    public LB2Device(String host, String mac, Lb2StateCmdPayloadFactory commandFactory) throws IOException {
        super(BLDevice.DEV_SP1, BLDevice.DESC_SP1, host, reverseMac(mac));

        this.mac = mac;
        this.host = host;
        this.commandFactory = commandFactory;
    }

    public void changeState(State state) throws IOException {
        var cmd = commandFactory.writeCommand(state);
        sendCmdPkt(cmd);
    }

    public String mac() {
        return mac;
    }

    public String host() {
        return host;
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
