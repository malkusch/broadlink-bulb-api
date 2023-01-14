package de.malkusch.broadlinkBulb.test;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

public final class UdpServer implements AutoCloseable {

    private final DatagramSocket socket;
    private final int bufferSize;
    private final List<byte[]> received = synchronizedList(new ArrayList<>());
    private final Queue<byte[]> responses;
    private final ExecutorService executor = newSingleThreadExecutor();

    public UdpServer(int port, int bufferSize, List<byte[]> responses) throws SocketException {
        socket = new DatagramSocket(port);
        this.bufferSize = bufferSize;
        this.responses = new LinkedList<>(responses);
        executor.execute(this::start);
    }

    public static final byte[] EMPTY_RESPONSE = new byte[0];

    public static final byte[] DISCOVER_RESPONSE = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, -24, 0, 0, 0, -25, 7, 43, 11,
            23, 5, 14, 1, 0, 0, 0, 0, -64, -88, -68, 73, 0, 0, 0, 0, 44, -44, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            5, 43, -57, 98, -56, 96, 105, -68, -88, -64, -62, -7, 80, -82, 11, -20 };

    public static final byte[] AUTH_RESPONSE = new byte[] { 90, -91, -86, 85, 90, -91, -86, 85, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -73, -40, 0, 0, 42, 39, -23, 3, 33, -57, -62, -7, 80, -82, 11,
            -20, 0, 0, 0, 0, 80, -58, 0, 0, 9, -97, -22, 25, -49, 16, 76, 122, -42, -106, -83, 70, 65, 95, 89, 105, 97,
            -62, 28, -38, -63, 103, 57, 97, 127, -40, 56, -93, -60, -81, 85, 59 };

    public static UdpServer server(byte[]... responses) throws SocketException {
        return new UdpServer(80, 255, asList(responses));
    }

    public byte[] nextPacket() {
        return received.remove(0);
    }

    private void start() {
        final byte[] buffer = new byte[bufferSize];
        while (!socket.isClosed()) {
            var packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                var data = copyOf(packet.getData(), packet.getLength());
                received.add(data);

                var response = responses.poll();
                if (response != null) {
                    socket.send(new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort()));
                }

            } catch (IOException e) {
                if (!socket.isClosed()) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (!socket.isClosed()) {
            socket.close();
        }
        if (!executor.isShutdown()) {
            executor.shutdown();
            executor.awaitTermination(3, SECONDS);
        }
    }
}