package de.malkusch.broadlinkBulb;

import static de.malkusch.broadlinkBulb.mob41.lb1.State.ColorMode.RGB;
import static de.malkusch.broadlinkBulb.mob41.lb1.State.ColorMode.WHITE;
import static de.malkusch.broadlinkBulb.mob41.lb1.State.Power.OFF;
import static de.malkusch.broadlinkBulb.mob41.lb1.State.Power.ON;

import java.io.IOException;

import de.malkusch.broadlinkBulb.mob41.lb1.LB1Device;
import de.malkusch.broadlinkBulb.mob41.lb1.State;

public final class BroadlinkBulb implements AutoCloseable {

    private final LB1Device device;

    /**
     * Use BroadlinkBulbFactory to create instances of BroadlinkBulb.
     * 
     * @author malkusch
     */
    BroadlinkBulb(LB1Device device) throws IOException {
        this.device = device;
        if (!device.auth()) {
            throw new IllegalArgumentException("Failed to authenticate");
        }
    }

    public String mac() {
        return device.mac();
    }

    public String host() {
        return device.host();
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", host(), mac());
    }

    public void turnOff() throws IOException {
        State state = new State();
        state.pwr = OFF;
        device.changeState(state);
    }

    public void turnOn() throws IOException {
        State state = new State();
        state.pwr = ON;
        device.changeState(state);
    }

    public boolean isOn() throws IOException {
        var state = device.readState();
        return state.pwr == ON;
    }

    public void dimm(Brightness brightness) throws IOException {
        State state = new State();
        state.brightness = brightness.value();
        device.changeState(state);
    }

    public Brightness brightness() throws IOException {
        var state = device.readState();
        return new Brightness(state.brightness);
    }

    public void shineWhite() throws IOException {
        State state = new State();
        state.bulb_colormode = WHITE;
        device.changeState(state);
    }

    public boolean isWhite() throws IOException {
        var state = device.readState();
        return state.bulb_colormode == WHITE;
    }

    public void changeColor(Color color) throws IOException {
        State state = new State();
        state.red = color.red().value();
        state.green = color.green().value();
        state.blue = color.blue().value();
        state.bulb_colormode = RGB;
        device.changeState(state);
    }

    public boolean isColor() throws IOException {
        var state = device.readState();
        return state.bulb_colormode == RGB;
    }

    public Color color() throws IOException {
        var state = device.readState();
        var color = new Color(state.red, state.green, state.blue);
        return color;
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BroadlinkBulb)) {
            return false;
        }
        var otherLight = (BroadlinkBulb) other;
        return device.equals(otherLight.device);
    }

    @Override
    public void close() {
        device.close();
    }
}
