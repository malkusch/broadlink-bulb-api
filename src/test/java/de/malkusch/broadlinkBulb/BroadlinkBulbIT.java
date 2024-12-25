package de.malkusch.broadlinkBulb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BroadlinkBulbIT {

    private static BroadlinkBulb light;

    @BeforeAll
    static void discoverLight() throws Exception {
        var factory = new BroadlinkBulbFactory(Duration.ofMillis(300));
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

        var brightness = new Brightness(10);
        light.dimm(brightness);
        assertEquals(brightness, light.brightness());

        light.shineWhite();
        assertTrue(light.isWhite());

        light.dimm(Brightness.FULL);
        assertEquals(Brightness.FULL, light.brightness());
    }
}
