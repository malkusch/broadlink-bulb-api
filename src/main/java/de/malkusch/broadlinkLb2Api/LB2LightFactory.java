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

import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.dis.DiscoveryPacket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LB2LightFactory {

    private static byte DEVICE_TYPE = -56;
    private final Duration timeout;

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

    public Collection<LB2Light> discover(InetAddress sourceIpAddr) throws IOException {
        var lights = new ArrayList<LB2Light>();
        try (var connection = new Connection(sourceIpAddr, InetAddress.getByName("255.255.255.255"), timeout, true)) {

            for (var next = connection.readNext(); next.isPresent(); next = connection.readNext()) {
                var light = next.get();
                log.info("Discovered {}", light);
                lights.add(light);
            }
        }
        return lights;
    }

    public LB2Light build(InetAddress target) throws IOException {
        try (var connection = Connection.connection(target, timeout, false)) {
            var light = connection.readNext()
                    .orElseThrow(() -> new IOException(String.format("Could not discover device {}", target)));
            return light;
        }
    }

    @Slf4j
    private static final class Connection implements AutoCloseable {
        private final InetAddress sourceIpAddr;
        private final int sourcePort = 0;
        private final DiscoveryPacket dpkt;
        private final DatagramSocket sock;

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

            this.sock = sock;
            sock.setSoTimeout((int) timeout.toMillis());
            if (broadcast) {
                sock.setBroadcast(true);
                sock.setReuseAddress(true);
            }
            log.debug("Discover from {} at {}", sourceIpAddr, target);

            dpkt = new DiscoveryPacket(sourceIpAddr, sourcePort);
            byte[] sendBytes = dpkt.getData();
            DatagramPacket sendpack = new DatagramPacket(sendBytes, sendBytes.length, target, DISCOVERY_DEST_PORT);

            sock.send(sendpack);
        }

        private final byte[] receBytes = new byte[DISCOVERY_RECEIVE_BUFFER_SIZE];

        private Optional<DatagramPacket> readNextPacket() throws IOException {
            DatagramPacket recePacket = new DatagramPacket(receBytes, 0, receBytes.length);
            try {
                sock.receive(recePacket);
                return Optional.of(recePacket);

            } catch (SocketTimeoutException e) {
                log.debug("Stop discovery at {}", sourceIpAddr);
                return Optional.empty();
            }
        }

        private Optional<LB2Light> readNext() throws IOException {
            var next = readNextPacket();
            if (next.isEmpty()) {
                return Optional.empty();
            }
            var recePacket = next.get();

            String host = recePacket.getAddress().getHostAddress();
            Mac mac = new Mac(reverseBytes(subbytes(receBytes, 0x3a, 0x40)));
            short deviceType = (short) (receBytes[0x34] | receBytes[0x35] << 8);

            log.debug("Info: host=" + host + " mac=" + mac.getMacString() + " deviceType=0x"
                    + Integer.toHexString(deviceType));
            log.debug("Creating BLDevice instance");

            if (deviceType != DEVICE_TYPE) {
                log.info("{} is unsupported device type {}", host, deviceType);
                return Optional.empty();
            }

            return Optional.of(new LB2Light(host, mac.toString()));
        }

        @Override
        public void close() {
            sock.close();
        }
    }
}
