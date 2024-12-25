package de.malkusch.broadlinkBulb;

public record Brightness(int value) {

    public static final Brightness FULL = new Brightness(100);

    public Brightness {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Brightness must be between 0 and 100");
        }
    }
}
