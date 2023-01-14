package de.malkusch.broadlinkBulb;

import static de.malkusch.broadlinkBulb.test.UdpServer.AUTH_RESPONSE;
import static de.malkusch.broadlinkBulb.test.UdpServer.DISCOVER_RESPONSE;
import static de.malkusch.broadlinkBulb.test.UdpServer.server;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BroadlinkBulbFactoryTest {

    private final BroadlinkBulbFactory factory = new BroadlinkBulbFactory();

    @ParameterizedTest
    @ValueSource(strings = { "localhost", "192.0.2.1", "example.org" })
    void buildShouldFail(String host) {
        assertThrows(IOException.class, () -> factory.build(host));
    }

    @Test
    void shouldBuild() throws Exception {
        try (var server = server(DISCOVER_RESPONSE, AUTH_RESPONSE)) {
            var bulb = factory.build("127.0.0.1");

            assertEquals("127.0.0.1", bulb.host());
            assertEquals("ec:0b:ae:50:f9:c2", bulb.mac());
        }
    }
}
