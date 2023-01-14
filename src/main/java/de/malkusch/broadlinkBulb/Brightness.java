package de.malkusch.broadlinkBulb;

public record Brightness(int value) {

    public Brightness {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Brightness must be between 0 and 255");
        }
    }
}
