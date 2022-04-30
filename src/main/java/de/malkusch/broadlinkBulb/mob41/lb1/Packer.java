package de.malkusch.broadlinkBulb.mob41.lb1;

import static com.igormaznitsa.jbbp.io.JBBPByteOrder.LITTLE_ENDIAN;
import static com.igormaznitsa.jbbp.io.JBBPOut.BeginBin;
import static com.igormaznitsa.jbbp.mapper.BinType.BYTE;
import static com.igormaznitsa.jbbp.mapper.BinType.INT;
import static com.igormaznitsa.jbbp.mapper.BinType.USHORT;

import java.io.IOException;

import com.igormaznitsa.jbbp.mapper.Bin;
import com.igormaznitsa.jbbp.mapper.BinType;

final class Packer {

    public byte[] pack(int p_len, int flag, long data_length) {
        var struct = new Struct();
        struct.p_len = p_len;
        struct.flag = flag;
        struct.data_length = data_length;

        try {
            return BeginBin().Bin(struct).End().toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to pack " + struct, e);
        }
    }

    private static class Struct {

        @Bin(order = 1, type = USHORT, byteOrder = LITTLE_ENDIAN)
        int p_len;

        @Bin(order = 3, type = BinType.BYTE_ARRAY, byteOrder = LITTLE_ENDIAN)
        byte[] const1 = { (byte) 0xa5, (byte) 0xa5 };

        @Bin(order = 3, type = BinType.BYTE_ARRAY, byteOrder = LITTLE_ENDIAN)
        byte[] const2 = { (byte) 0x5A, (byte) 0x5A };

        @Bin(order = 4, type = USHORT, byteOrder = LITTLE_ENDIAN)
        int const3 = 0;

        @Bin(order = 5, type = BYTE, byteOrder = LITTLE_ENDIAN)
        int flag;

        @Bin(order = 6, type = BYTE, byteOrder = LITTLE_ENDIAN)
        byte const4 = (byte) 0x0B;

        @Bin(order = 6, type = INT, byteOrder = LITTLE_ENDIAN)
        long data_length;
    }
}
