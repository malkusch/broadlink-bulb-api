package de.malkusch.broadlinkLb2Api;

import static com.github.mob41.blapi.BLDevice.DISCOVERY_DEST_PORT;
import static com.github.mob41.blapi.BLDevice.DISCOVERY_RECEIVE_BUFFER_SIZE;
import static com.github.mob41.blapi.BLDevice.reverseBytes;
import static com.github.mob41.blapi.BLDevice.subbytes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.dis.DiscoveryPacket;

import de.malkusch.broadlinkLb2Api.mob41.lb2.Lb2StateCmdPayloadFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LB2LightFactory {

    private static byte DEVICE_TYPE = -56;
    private final Duration timeout;
    private final Lb2StateCmdPayloadFactory commandFactory = new Lb2StateCmdPayloadFactory(new ObjectMapper());

    public LB2LightFactory() {
        this(Duration.ofSeconds(5));
    }

    public Collection<LB2Light> discover() throws IOException {
        var addresses = NetworkInterface.networkInterfaces().flatMap(it -> it.getInterfaceAddresses().stream())
                .map(it -> it.getAddress()) //
                .filter(it -> it instanceof Inet4Address) //
                .filter(it -> !it.isLoopbackAddress()) //
                .filter(it -> !it.isLinkLocalAddress()) //
                .toList();
        var lights = new ArrayList<LB2Light>();
        for (var address : addresses) {
            lights.addAll(discover(address));
        }
        return lights;
    }

    public Collection<LB2Light> discover(String sourceIpAddr) throws IOException {
        return discover(InetAddress.getByName(sourceIpAddr));
    }

    public Collection<LB2Light> discover(InetAddress sourceIpAddr) throws IOException {
        var lights = new ArrayList<LB2Light>();
        try (var connection = new Connection(sourceIpAddr, InetAddress.getByName("255.255.255.255"), timeout, true)) {

            for (var next = connection.readNext(); next.isPresent(); next = connection.readNext()) {
                var response = next.get();
                var light = build(response);
                log.info("Discovered {}", light);
                lights.add(light);
            }
        }
        return lights;
    }

    public LB2Light build(String target) throws IOException {
        return build(target);
    }

    public LB2Light build(InetAddress target) throws IOException {
        try (var connection = Connection.connection(target, timeout, false)) {
            var response = connection.readNext()
                    .orElseThrow(() -> new IOException(String.format("Could not discover device %s", target)));
            return build(response);
        }
    }

    private LB2Light build(Connection.Response response) throws IOException {
        return new LB2Light(response.host, response.mac, commandFactory);
    }

    @Slf4j
    private static final class Connection implements AutoCloseable {
        private final InetAddress sourceIpAddr;
        private final int sourcePort = 0;
        private final DatagramSocket socket;

        public static Connection connection(InetAddress target, Duration timeout, boolean broadcast)
                throws IOException {

            var sock = new DatagramSocket();
            sock.connect(target, DISCOVERY_DEST_PORT);
            var source = sock.getLocalAddress();

            return new Connection(sock, source, target, timeout, broadcast);
        }

        public Connection(InetAddress source, InetAddress target, Duration timeout, boolean broadcast)
                throws IOException {

            this(new DatagramSocket(0, source), source, target, timeout, broadcast);
        }

        private Connection(DatagramSocket sock, InetAddress source, InetAddress target, Duration timeout,
                boolean broadcast) throws IOException {

            sourceIpAddr = source;

            this.socket = sock;
            sock.setSoTimeout((int) timeout.toMillis());
            if (broadcast) {
                sock.setBroadcast(true);
                sock.setReuseAddress(true);
            }
            log.debug("Discover from {} at {}", sourceIpAddr, target);

            DiscoveryPacket dpkt = new DiscoveryPacket(sourceIpAddr, sourcePort);
            byte[] sendBytes = dpkt.getData();
            DatagramPacket sendpack = new DatagramPacket(sendBytes, sendBytes.length, target, DISCOVERY_DEST_PORT);

            sock.send(sendpack);
        }

        private final byte[] readBuffer = new byte[DISCOVERY_RECEIVE_BUFFER_SIZE];

        private Optional<DatagramPacket> readNextPacket() throws IOException {
            DatagramPacket packet = new DatagramPacket(readBuffer, 0, readBuffer.length);
            try {
                socket.receive(packet);
                return Optional.of(packet);

            } catch (SocketTimeoutException e) {
                log.debug("Stop discovery at {}", sourceIpAddr);
                return Optional.empty();
            }
        }

        private static record Response(String host, String mac) {

        }

        public Optional<Response> readNext() throws IOException {
            var next = readNextPacket();
            if (next.isEmpty()) {
                return Optional.empty();
            }
            var recePacket = next.get();

            String host = recePacket.getAddress().getHostAddress();
            Mac mac = new Mac(reverseBytes(subbytes(readBuffer, 0x3a, 0x40)));
            short deviceType = (short) (readBuffer[0x34] | readBuffer[0x35] << 8);

            log.debug("Info: host=" + host + " mac=" + mac.getMacString() + " deviceType=0x"
                    + Integer.toHexString(deviceType));
            log.debug("Creating BLDevice instance");

            if (deviceType != DEVICE_TYPE) {
                log.info("{} is unsupported device type {}", host, deviceType);
                return Optional.empty();
            }

            return Optional.of(new Response(host, mac.toString()));
        }

        @Override
        public void close() {
            socket.close();
        }
    }
}
