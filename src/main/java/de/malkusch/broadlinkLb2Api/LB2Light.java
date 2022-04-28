package de.malkusch.broadlinkLb2Api;

import java.io.IOException;

import com.github.mob41.blapi.SP1Device;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.mac.MacFormatException;

import de.malkusch.broadlinkLb2Api.mob41.lb2.Lb2StateCmdPayloadFactory;
import de.malkusch.broadlinkLb2Api.mob41.lb2.State;
import de.malkusch.broadlinkLb2Api.mob41.lb2.State.ColorMode;
import de.malkusch.broadlinkLb2Api.mob41.lb2.State.Power;

public final class LB2Light implements AutoCloseable {

    private final Lb2StateCmdPayloadFactory commandFactory;
    private final SP1Device device;
    private final String host;
    private final String mac;

    /**
     * Use LB2LightFactory to create instances of LB2Light.
     * 
     * @author malkusch
     */
    LB2Light(String host, String mac, Lb2StateCmdPayloadFactory commandFactory) throws IOException {
        try {
            this.host = host;
            this.mac = mac;
            device = new SP1Device(host, reverseMac(mac));
            this.commandFactory = commandFactory;

        } catch (MacFormatException e) {
            throw new IllegalArgumentException(String.format("Mac '{}' is invalid", mac), e);
        }
        if (!device.auth()) {
            throw new IllegalArgumentException("Failed to authenticate");
        }
    }

    public String mac() {
        return mac;
    }

    public String host() {
        return host;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", host, mac);
    }

    public void turnOff() throws IOException {
        State state = new State(Power.OFF, null, null, null, null, null);
        changeState(state);
    }

    public void turnOn() throws IOException {
        State state = new State(Power.ON, null, null, null, null, null);
        changeState(state);
    }

    public void dimm(Brightness brightness) throws IOException {
        State state = new State(null, null, null, null, brightness.value(), null);
        changeState(state);
    }

    public void shineWhite() throws IOException {
        State state = new State(null, null, null, null, null, ColorMode.WHITE);
        changeState(state);
    }

    public void changeColor(Color color) throws IOException {
        State state = new State(null, color.red().value(), color.green().value(), color.blue().value(), null,
                ColorMode.RGB);
        changeState(state);
    }

    private void changeState(State state) throws IOException {
        var cmd = commandFactory.build(state);
        device.sendCmdPkt(cmd);
    }

    private static Mac reverseMac(String mac) {
        var bytes = Mac.macStrToBytes(mac);
        var reversed = new byte[6];
        reversed[0] = bytes[5];
        reversed[1] = bytes[4];
        reversed[2] = bytes[3];
        reversed[3] = bytes[2];
        reversed[4] = bytes[1];
        reversed[5] = bytes[0];

        return new Mac(reversed);
    }

    @Override
    public void close() {
        device.close();
    }
}
