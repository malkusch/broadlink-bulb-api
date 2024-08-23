package de.malkusch.broadlinkBulb.mob41.lb1;

import com.github.mob41.blapi.pkt.CmdPayload;
import com.github.mob41.blapi.pkt.Payload;

import static java.util.Objects.requireNonNull;

public class Lb2StateCmdPayload implements CmdPayload {

    private final Payload payload;

    public Lb2StateCmdPayload(Payload payload) {
        this.payload = requireNonNull(payload);
    }

    @Override
    public byte getCommand() {
        return 0x6A;
    }

    @Override
    public Payload getPayload() {
        return payload;
    }

}
