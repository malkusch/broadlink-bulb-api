package com.github.mob41.blapi.lb2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class PackerTest {

    private final Packer packer = new Packer();

    private static record Scenario(String expectation, int p_len, int flag, long data_length) {
    }

    public static Scenario[] shouldPack() {
        return new Scenario[] { new Scenario("3300a5a55a5a0000010b27000000", 51, 1, 39),
                new Scenario("ff00a5a55a5a0000020bffffffff", 255, 2, 4294967295L),
                new Scenario("2600a5a55a5a0000020b27000000", 38, 2, 39),
                new Scenario("0000a5a55a5a0000010b27000000", 0, 1, 39),
                new Scenario("0100a5a55a5a0000010b27000000", 1, 1, 39),
                new Scenario("ff00a5a55a5a0000010b27000000", 255, 1, 39),
                new Scenario("2600a5a55a5a0000010b00000000", 38, 1, 0),
                new Scenario("2600a5a55a5a0000010b01000000", 38, 1, 1),
                new Scenario("2600a5a55a5a0000010bffffffff", 38, 1, 4294967295L) };
    };

    @ParameterizedTest
    @MethodSource
    void shouldPack(Scenario scenario) {
        var packed = packer.pack(scenario.p_len, scenario.flag, scenario.data_length);

        assertEquals(scenario.expectation, Hex.encodeHexString(packed));
    }
}
