
package com.github.mob41.blapi.pkt;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.BLDevice;
import com.github.mob41.blapi.Checksum;
import com.github.mob41.blapi.ex.BLApiRuntimeException;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.auth.AES;

/**
 * This constructs a byte array with the format of a command to the Broadlink
 * device
 * 
 * @author Anthony
 *
 */
public class CmdPacket implements Packet {

    private static final Logger log = LoggerFactory.getLogger(CmdPacket.class);

    private final byte[] data;

    /**
     * Constructs a command packet
     * 
     * @param targetMac
     *            Target Broadlink device MAC address
     * @param count
     *            Count of packets sent (provided by BLDevice sendPkt())
     * @param id
     *            This BLDevice ID provided by the Broadlink device. It is
     *            {0,0,0,0} if auth() not ran
     * @param aesInstance
     *            The AES encrypt/decrypt instance
     * @param cmdPayload
     *            The data to be sent
     */
    public CmdPacket(Mac targetMac, int count, byte[] id, AES aesInstance, CmdPayload cmdPayload) {

        byte cmd = cmdPayload.getCommand();
        byte[] payload = cmdPayload.getPayload().getData();
        byte[] headerdata;

        log.debug("Constructor CmdPacket starts");
        log.debug("count=" + count + " cmdPayload.cmd=" + Integer.toHexString(cmd) + " payload.len=" + payload.length);

        count = (count + 1) & 0xffff; // increased by the sendPkt()

        log.debug("New count: " + count + " (added by 1)");
        log.debug("Creating byte array with data");

        headerdata = new byte[BLDevice.DEFAULT_BYTES_SIZE];
        for (int i = 0; i < headerdata.length; i++) {
            headerdata[i] = 0x00;
        }

        headerdata[0x00] = 0x5a;
        headerdata[0x01] = (byte) 0xa5;
        headerdata[0x02] = (byte) 0xaa;
        headerdata[0x03] = 0x55;
        headerdata[0x04] = 0x5a;
        headerdata[0x05] = (byte) 0xa5;
        headerdata[0x06] = (byte) 0xaa;
        headerdata[0x07] = 0x55;

        //headerdata[0x24] = (byte) 0xc8;
        //headerdata[0x25] = 0x60;
        
        headerdata[0x24] = 0x2a;
        headerdata[0x25] = 0x27;
        headerdata[0x26] = cmd;

        headerdata[0x28] = (byte) (count & 0xff);
        headerdata[0x29] = (byte) (count >> 8);

        byte[] mac = targetMac.getMac();

        headerdata[0x2a] = mac[5];
        headerdata[0x2b] = mac[4];
        headerdata[0x2c] = mac[3];
        headerdata[0x2d] = mac[2];
        headerdata[0x2e] = mac[1];
        headerdata[0x2f] = mac[0];
        
        headerdata[0x30] = id[0];
        headerdata[0x31] = id[1];
        headerdata[0x32] = id[2];
        headerdata[0x33] = id[3];

        // pad the payload for AES encryption
        byte[] payloadPad = null;
        if(payload.length > 0) {
          int numpad = 16 - (payload.length % 16);

          payloadPad = new byte[payload.length+numpad];
          for(int i = 0; i < payloadPad.length; i++) {
              if(i < payload.length)
                  payloadPad[i] = payload[i];
              else
                  payloadPad[i] = 0x00;
          }
        }

        log.debug("Running checksum for un-encrypted payload");
        var checksum = Checksum.checksum(payloadPad);
        headerdata[0x34] = checksum[0];
        headerdata[0x35] = checksum[1];

        try {
            log.debug("Encrypting payload");

            payload = aesInstance.encrypt(payloadPad);
            log.debug("Encrypted payload bytes: {}", DatatypeConverter.printHexBinary(payload));

            log.debug("Encrypted. len=" + payload.length);
        } catch (Exception e) {
            log.error("Cannot encrypt payload! Aborting", e);
            throw new BLApiRuntimeException("Cannot encrypt payload", e);
        }

        data = new byte[BLDevice.DEFAULT_BYTES_SIZE + payload.length];
        
        for (int i = 0; i < headerdata.length; i++) {
            data[i] = headerdata[i];
        }

        for (int i = 0; i < payload.length; i++) {
            data[i + BLDevice.DEFAULT_BYTES_SIZE] = payload[i];
        }

        log.debug("Running whole packet checksum");

        int checksumpkt = 0xbeaf;
        for (int i = 0; i < data.length; i++) {
            checksumpkt = checksumpkt + Byte.toUnsignedInt(data[i]);
            checksumpkt = checksumpkt & 0xffff;
//            log.debug("index: " + i + ", data byte: " + Byte.toUnsignedInt(data[i]) + ", checksum: " + checksumpkt);
        }

        log.debug("Whole packet checksum: " + Integer.toHexString(checksumpkt));

        data[0x20] = (byte) (checksumpkt & 0xff);
        data[0x21] = (byte) (checksumpkt >> 8);

        log.debug("End of CmdPacket constructor");
    }

    @Override
    public byte[] getData() {
        return data;
    }

}
