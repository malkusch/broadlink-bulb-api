package com.github.mob41.blapi.lb2;

import com.github.mob41.blapi.pkt.CmdPayload;
import com.github.mob41.blapi.pkt.Payload;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Lb2StateCmdPayload implements CmdPayload {

    private final Payload payload;

    @Override
    public byte getCommand() {
        return 0x6A;
    }

    @Override
    public Payload getPayload() {
        return payload;
    }

}
