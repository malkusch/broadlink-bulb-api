package de.malkusch.broadlinkLb2Api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LB2LightIT {

    private static LB2Light light;

    @BeforeAll
    private static void discoverLight() throws Exception {
        var factory = new LB2LightFactory(Duration.ofMillis(300));
        var lights = factory.discover();
        light = lights.stream().findFirst().orElse(null);

        assumeTrue(light != null);
    }

    @Test
    public void testStates() throws Exception {
        light.turnOn();
        assertTrue(light.isOn());

        light.turnOff();
        assertFalse(light.isOn());

        light.shineWhite();
        assertTrue(light.isWhite());

        var color = new Color(1, 2, 3);
        light.changeColor(color);
        assertTrue(light.isColor());
        assertEquals(color, light.color());

        var brightness = new Brightness(100);
        light.dimm(brightness);
        assertEquals(brightness, light.brightness());
    }
}
