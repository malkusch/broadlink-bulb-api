package de.malkusch.broadlinkLb2Api.mob41.lb2;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.broadlinkLb2Api.mob41.lb2.State.ColorMode;
import de.malkusch.broadlinkLb2Api.mob41.lb2.State.Power;

public class Lb2StateCmdPayloadFactoryTest {

    private final Lb2StateCmdPayloadFactory factory = new Lb2StateCmdPayloadFactory(new ObjectMapper());

    public static Object[][] testSerialization_data() {
        return new Object[][] { //
                { new State(Power.ON, null, null, null, null, null), "{\"pwr\":1}" }, //
                { new State(Power.OFF, null, null, null, null, null), "{\"pwr\":0}" }, //
                { new State(null, null, null, null, null, ColorMode.RGB), "{\"bulb_colormode\":0}" }, //
                { new State(null, null, null, null, null, ColorMode.WHITE), "{\"bulb_colormode\":1}" }, //
                { new State(null, 1, 2, 3, 4, null), "{\"red\":1,\"green\":2,\"blue\":3,\"brightness\":4}" }, //
                { new State(null, 0, 0, 0, 0, null), "{\"red\":0,\"green\":0,\"blue\":0,\"brightness\":0}" }, //
        };
    }

    @ParameterizedTest
    @MethodSource("testSerialization_data")
    public void testSerialization(State state, String expected) throws Exception {
        var json = factory.json(state);

        JSONAssert.assertEquals(expected, new String(json), JSONCompareMode.STRICT);
    }

}
