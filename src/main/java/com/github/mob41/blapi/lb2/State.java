package com.github.mob41.blapi.lb2;

import com.fasterxml.jackson.annotation.JsonValue;
import com.github.mob41.blapi.lb2.State.ColorMode;
import com.github.mob41.blapi.lb2.State.Power;

import lombok.RequiredArgsConstructor;

/*public record State(int pwr, int red, int blue, int green, int brightness, int colortemp, int hue, int saturation,
        int transitionduration, int maxworktime, int bulb_colormode, String bulb_scenes, String bulb_scene,
        int bulb_sceneidx) {
}
*/

public record State(Power pwr, int red, int blue, int green, ColorMode bulb_colormode) {

    @RequiredArgsConstructor
    public static enum Power {
        ON(1), OFF(0);

        @JsonValue
        private final int value;
    }

    @RequiredArgsConstructor
    public static enum ColorMode {
        RGB(0), WHITE(1), SCENE(2);

        @JsonValue
        private final int value;
    }
}
