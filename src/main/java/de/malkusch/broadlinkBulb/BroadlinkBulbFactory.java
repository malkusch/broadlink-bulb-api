package de.malkusch.broadlinkBulb;

import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.dis.DiscoveryPacket;
import de.malkusch.broadlinkBulb.mob41.lb1.Codec;
import de.malkusch.broadlinkBulb.mob41.lb1.LB1Device;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static com.github.mob41.blapi.BLDevice.*;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Objects.requireNonNull;

/**
 * Discovers or builds BroadlinkBulb devices.
 *
 * E.g. discover all devices: <code>
 * var factory = new BroadlinkBulbFactory();
 * var lights = factory.discover();
 * for (var light : lights) {
 *   light.turnOn();
 * }
 * </code>
 *
 * @author malkusch
 */
public final class BroadlinkBulbFactory {

    private static final System.Logger log = System.getLogger(BroadlinkBulbFactory.class.getName());
    private final Duration timeout;
    private final Codec codec = new Codec();

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    public BroadlinkBulbFactory(Duration timeout) {
        this.timeout = requireNonNull(timeout);
    }

    public BroadlinkBulbFactory() {
        this(DEFAULT_TIMEOUT);
    }

    /**
     * Discovers all devices in the LAN.
     *
     * @return
     * @throws IOException
     */
    public Collection<BroadlinkBulb> discover() throws IOException {
        var addresses = NetworkInterface.networkInterfaces().flatMap(it -> it.getInterfaceAddresses().stream())
                .map(it -> it.getAddress()) //
                .filter(it -> it instanceof Inet4Address) //
                .filter(it -> !it.isLoopbackAddress()) //
                .filter(it -> !it.isLinkLocalAddress()) //
                .toList();
        var lights = new ArrayList<BroadlinkBulb>();
        for (var address : addresses) {
            lights.addAll(discover(address));
        }
        return lights;
    }

    /**
     * Discovers all devices in the LAN.
     *
     * This discovery is limited to one network device.
     *
     * @param source
     *            The source Ip address of a network interface
     */
    public Collection<BroadlinkBulb> discover(String source) throws IOException {
        return discover(InetAddress.getByName(source));
    }

    /**
     * Discovers all devices in the LAN.
     *
     * This discovery is limited to one network device.
     *
     * @param source
     *            The source Ip address of a network interface
     */
    public Collection<BroadlinkBulb> discover(InetAddress source) throws IOException {
        var lights = new ArrayList<BroadlinkBulb>();
        try (var connection = new Connection(source, InetAddress.getByName("255.255.255.255"), timeout, true)) {

            for (var next = connection.readNext(); next.isPresent(); next = connection.readNext()) {
                var response = next.get();
                var light = build(response);
                log.log(INFO, "Discovered {0}", light);
                lights.add(light);
            }
        }
        return lights;
    }

    /**
     * Connect to a known device
     *
     * @param target
     *            hostname
     */
    public BroadlinkBulb build(String target) throws IOException {
        return build(InetAddress.getByName(target));
    }

    /**
     * Connect to a known device
     *
     * @param target
     *            The device's ip address
     */
    public BroadlinkBulb build(InetAddress target) throws IOException {
        try (var connection = Connection.connection(target, timeout, false)) {
            var response = connection.readNext()
                    .orElseThrow(() -> new IOException(String.format("Could not discover device %s", target)));
            return build(response);
        }
    }

    private BroadlinkBulb build(Connection.Response response) throws IOException {
        var device = new LB1Device(response.host, response.mac, timeout, codec);
        return new BroadlinkBulb(device);
    }

    private static final class Connection implements AutoCloseable {

        private static final System.Logger log = System.getLogger(Connection.class.getName());
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
            log.log(DEBUG, "Discover from {0} at {1}", sourceIpAddr, target);

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
                log.log(DEBUG, "Stop discovery at {0}", sourceIpAddr);
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

            log.log(DEBUG, "Info: host=" + host + " mac=" + mac.getMacString() + " deviceType=0x"
                    + Integer.toHexString(deviceType));
            log.log(DEBUG, "Creating BLDevice instance");

            if (deviceType != LB1Device.DEVICE_TYPE) {
                log.log(DEBUG, "{0} is unsupported device type {1}", host, deviceType);
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
