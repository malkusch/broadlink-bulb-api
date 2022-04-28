package de.malkusch.broadlinkLb2Api.mob41;

public final class Checksum {

    public static byte[] checksum(byte[] data) {
        return checksum(data, 0);
    }
    
    public static byte[] checksum(byte[] data, int offset) {
        int checksumpayload = 0xbeaf;
        for (int i = offset; i < data.length; i++) {
            checksumpayload = checksumpayload + Byte.toUnsignedInt(data[i]);
            checksumpayload = checksumpayload & 0xffff;
        }
        return new byte[] { (byte) (checksumpayload & 0xff), (byte) (checksumpayload >> 8) };
    }
}
