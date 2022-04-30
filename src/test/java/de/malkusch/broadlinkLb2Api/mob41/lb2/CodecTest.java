package de.malkusch.broadlinkLb2Api.mob41.lb2;

import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import de.malkusch.broadlinkLb2Api.mob41.lb2.State.ColorMode;
import de.malkusch.broadlinkLb2Api.mob41.lb2.State.Power;

public class CodecTest {

    private final Codec codec = new Codec();

    public static Object[][] testSerialization_data() {
        return new Object[][] { //
                { state(it -> it.pwr = Power.ON), "{\"pwr\":1}" }, //
                { state(it -> it.pwr = Power.OFF), "{\"pwr\":0}" }, //
                { state(it -> it.bulb_colormode = ColorMode.RGB), "{\"bulb_colormode\":0}" }, //
                { state(it -> it.bulb_colormode = ColorMode.WHITE), "{\"bulb_colormode\":1}" }, //
                { state(it -> {
                    it.red = 1;
                    it.green = 2;
                    it.blue = 3;
                    it.brightness = 4;
                }), "{\"red\":1,\"green\":2,\"blue\":3,\"brightness\":4}" }, //
                { state(it -> {
                    it.red = 0;
                    it.green = 0;
                    it.blue = 0;
                    it.brightness = 0;
                }), "{\"red\":0,\"green\":0,\"blue\":0,\"brightness\":0}" }, //
        };
    }

    @ParameterizedTest
    @MethodSource("testSerialization_data")
    public void testSerialization(State state, String expected) throws Exception {
        var json = codec.json(state);

        JSONAssert.assertEquals(expected, new String(json), JSONCompareMode.STRICT);
    }

    private static State state(Consumer<State> setter) {
        var state = new State();
        setter.accept(state);
        return state;
    }
}
