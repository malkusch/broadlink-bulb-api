package de.malkusch.broadlinkLb2Api.mob41.lb2;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mob41.blapi.BLDevice;
import com.github.mob41.blapi.pkt.BytePayload;

import de.malkusch.broadlinkLb2Api.mob41.Checksum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Codec {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Unpacker unpacker = new Unpacker();
    private final Packer packer = new Packer();

    public State decode(byte[] response) throws IOException {
        var js_len = (int) unpacker.unpack_js_len(response);
        var json_data = BLDevice.subbytes(response, 14, 14 + js_len);
        log.debug("decoded: {}", new String(json_data));
        return mapper.readValue(json_data, State.class);
    }

    public Lb2StateCmdPayload encode(State state, int flag) throws IOException {
        try {
            var data = json(state);
            var p_len = 12 + data.length;
            var packet = packer.pack(p_len, flag, data.length);
            packet = extend(packet, data);
            var checksum = Checksum.checksum(packet, 2);
            packet[6] = checksum[0];
            packet[7] = checksum[1];

            return new Lb2StateCmdPayload(new BytePayload(packet));

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    byte[] json(State state) throws IOException {
        var json = mapper.writeValueAsBytes(state);
        log.debug("Serialized state to: {}", new String(json));
        return json;
    }

    private static byte[] extend(byte[] first, byte[] second) {
        byte[] extended = new byte[first.length + second.length];
        System.arraycopy(first, 0, extended, 0, first.length);
        System.arraycopy(second, 0, extended, first.length, second.length);
        return extended;
    }
}
