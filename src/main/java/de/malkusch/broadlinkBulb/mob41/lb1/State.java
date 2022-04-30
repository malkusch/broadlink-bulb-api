package de.malkusch.broadlinkBulb.mob41.lb1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public final class State {

    public Power pwr;
    public Integer red;
    public Integer green;
    public Integer blue;
    public Integer brightness;
    public ColorMode bulb_colormode;

    public static final State EMPTY = new State();

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

    Map<String, Object> toMap() {
        var map = new HashMap<String, Object>();
        map.put("pwr", pwr);
        map.put("red", red);
        map.put("green", green);
        map.put("blue", blue);
        map.put("brightness", brightness);
        map.put("bulb_colormode", bulb_colormode);
        return map;
    }
}
