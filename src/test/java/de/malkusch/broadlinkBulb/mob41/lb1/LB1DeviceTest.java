package de.malkusch.broadlinkBulb.mob41.lb1;

import static de.malkusch.broadlinkBulb.test.UdpServer.AUTH_RESPONSE;
import static de.malkusch.broadlinkBulb.test.UdpServer.EMPTY_RESPONSE;
import static de.malkusch.broadlinkBulb.test.UdpServer.server;
import static java.util.Arrays.copyOfRange;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LB1DeviceTest {

    LB1Device device;

    @BeforeEach
    public void setupDevice() throws IOException {
        var host = "localhost";
        var mac = "ec:0b:ae:50:f9:c2";
        var timeout = Duration.ofSeconds(1);
        var codec = new Codec();

        device = new LB1Device(host, mac, timeout, codec);
    }

    @AfterEach
    public void stopDevice() {
        device.close();
    }

    @Test
    public void authShouldSucceed() throws Exception {
        try (var server = server(AUTH_RESPONSE)) {
            var auth = device.auth();

            var packet = server.nextPacket();
            assertEquals(152, packet.length);
            assertArrayEquals(new byte[] { 90, -91, -86, 85, 90, -91, -86, 85, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, copyOfRange(packet, 0, 32));
            assertArrayEquals(new byte[] { -62, -7, 80, -82, 11, -20, 0, 0, 0, 0, -95, -61, 0, 0, 69, 52, 82, -25, -7,
                    46, -38, -107, -125, 68, -109, 8, 53, -17, -102, 109, -5, 105, 45, -61, 112, -71, 4, 67, -84, 92,
                    -42, 63, -69, 83, -83, -6, 8, -127, 76, -89, -8, -49, 65, 113, 0, 50, -114, 87, 12, 59, -122, -55,
                    77, 5, 112, -124, 73, -93, -119, -30, -102, -31, 4, 84, 54, -96, 91, -35, -36, 2, -63, 97, -81, 19,
                    37, -24, 126, 25, -80, -9, -47, -50, 6, -115, -27, 27, 97, -111, 86, -121, 109, 51, -116, -1, 59,
                    -103, 30, 64, -51, -79 }, copyOfRange(packet, 152 - 110, 152));
            assertTrue(auth);
        }
    }

    @Test
    public void authShouldFail() throws Exception {
        try (var server = server(EMPTY_RESPONSE)) {
            var auth = device.auth();
            assertFalse(auth);
        }
    }
}
