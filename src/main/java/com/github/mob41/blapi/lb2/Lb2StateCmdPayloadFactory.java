package com.github.mob41.blapi.lb2;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mob41.blapi.Checksum;
import com.github.mob41.blapi.pkt.BytePayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class Lb2StateCmdPayloadFactory {

    private final ObjectMapper mapper;
    private final Packer packer = new Packer();

    private static final int FLAG_WRITE = 2;
    private static final int FLAG_READ = 1;

    public Lb2StateCmdPayload build(State state) {
        try {
            var data = mapper.writeValueAsBytes(state);
            log.debug("Serialized state to: {}", new String(data));
            var p_len = 12 + data.length;
            var packet = packer.pack(p_len, FLAG_WRITE, data.length);
            packet = extend(packet, data);
            var checksum = Checksum.checksum(packet, 2);
            packet[6] = checksum[0];
            packet[7] = checksum[1];

            return new Lb2StateCmdPayload(new BytePayload(packet));

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] extend(byte[] first, byte[] second) {
        byte[] extended = new byte[first.length + second.length];
        System.arraycopy(first, 0, extended, 0, first.length);
        System.arraycopy(second, 0, extended, first.length, second.length);
        return extended;
    }

}
