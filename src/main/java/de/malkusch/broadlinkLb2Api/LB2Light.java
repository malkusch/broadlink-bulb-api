package de.malkusch.broadlinkLb2Api;

import java.io.IOException;

import de.malkusch.broadlinkLb2Api.mob41.lb2.LB2Device;
import de.malkusch.broadlinkLb2Api.mob41.lb2.State;
import de.malkusch.broadlinkLb2Api.mob41.lb2.State.ColorMode;
import de.malkusch.broadlinkLb2Api.mob41.lb2.State.Power;

public final class LB2Light implements AutoCloseable {

    private final LB2Device device;

    /**
     * Use LB2LightFactory to create instances of LB2Light.
     * 
     * @author malkusch
     */
    LB2Light(LB2Device device) throws IOException {
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
        State state = new State(Power.OFF, null, null, null, null, null);
        device.changeState(state);
    }

    public void turnOn() throws IOException {
        State state = new State(Power.ON, null, null, null, null, null);
        device.changeState(state);
    }

    public void dimm(Brightness brightness) throws IOException {
        State state = new State(null, null, null, null, brightness.value(), null);
        device.changeState(state);
    }

    public void shineWhite() throws IOException {
        State state = new State(null, null, null, null, null, ColorMode.WHITE);
        device.changeState(state);
    }

    public void changeColor(Color color) throws IOException {
        State state = new State(null, color.red().value(), color.green().value(), color.blue().value(), null,
                ColorMode.RGB);
        device.changeState(state);
    }

    @Override
    public void close() {
        device.close();
    }
}
