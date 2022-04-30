package de.malkusch.broadlinkLb2Api.mob41.lb2;

import java.io.IOException;

final class Unpacker {

    public long unpack_js_len(byte[] data) throws IOException {
        byte[] js_len_data = { data[10], data[11], data[12], data[13] };
        return toUInt(js_len_data);
    }

    private static long toUInt(final byte[] data) {
        if (data == null || data.length != 4)
            throw new IllegalArgumentException("!= 4 bytes");

        return (long) ((long) (data[3] & 0xffL) << 24 | (long) (data[2] & 0xffL) << 16 | (long) (data[1] & 0xffL) << 8
                | (long) (data[0] & 0xffL));
    }

}
