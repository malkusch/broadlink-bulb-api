package de.malkusch.broadlinkLb2Api.mob41.lb2;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import de.malkusch.broadlinkLb2Api.mob41.lb2.State.ColorMode;
import de.malkusch.broadlinkLb2Api.mob41.lb2.State.Power;
import lombok.RequiredArgsConstructor;

/*public record State(int pwr, int red, int blue, int green, int brightness, int colortemp, int hue, int saturation,
        int transitionduration, int maxworktime, int bulb_colormode, String bulb_scenes, String bulb_scene,
        int bulb_sceneidx) {
}
*/

/**
 * See
 * https://github.com/mjg59/python-broadlink/blob/2b70440786c7b63eb4445676db78a2acd387eaf4/broadlink/light.py#L131-L174
 */
@JsonInclude(NON_NULL)
public record State(Power pwr, Integer red, Integer green, Integer blue, Integer brightness, ColorMode bulb_colormode) {

    public static final State EMPTY = new State(null, null, null, null, null, null);

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
